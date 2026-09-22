package com.QuoocsCuongwf.EFEWallet.WalletService.aspect;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.IdempotencyKeyRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.IdempotencyKey;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.IdempotencyStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.IdempotencyException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.QuoocsCuongwf.EFEWallet.WalletService.annotation.Idempotent)")
    @Transactional
    public Object HandlIdemponcy(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String key=request.getHeader("Idempotency-Key");
        if (key == null ||key.isBlank() ){
            throw new InvalidAlgorithmParameterException("Missing idemponcy key");
        }
        String payload = extractRequestBody(joinPoint.getArgs());
        String requestHash = DigestUtils.sha256Hex(payload);
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String userId=auth.getName();
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findForUpdate(key,userId);
        if (existing.isPresent()) {
            if (!existing.get().getRequestHash().equals(requestHash)) {
                throw new IdempotencyException("Payload mismatch for existing idempotency key");
            }
            if (existing.get().getStatus() == IdempotencyStatus.SUCCESS && existing.get().getResponseBody() != null) {
                int responseStatus = existing.get().getResponseStatus() != null ? existing.get().getResponseStatus() : 200;
                Object cachedBody = objectMapper.readValue(existing.get().getResponseBody(), Map.class);
                return ResponseEntity.status(responseStatus).body(cachedBody);
            }
            if (existing.get().getStatus() == IdempotencyStatus.PROCESSING) {
                throw new IdempotencyException("Request with this idempotency key is being processed");
            }
            throw new IdempotencyException("Request with this idempotency key has failed before");
        }

        IdempotencyKey record = IdempotencyKey.builder()
                .idempotencyKey(key)
                .userId(userId)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .build();

        idempotencyKeyRepository.save(record);

        try {
            Object result = joinPoint.proceed();

            record.setStatus(IdempotencyStatus.SUCCESS);
            record.setResponseBody(objectMapper.writeValueAsString(result));
            record.setResponseStatus(200);

            idempotencyKeyRepository.save(record);

            return result;

        } catch (Exception e) {
            record.setStatus(IdempotencyStatus.FAILED);
            idempotencyKeyRepository.save(record);
            throw e;
        }
    }

    private String extractRequestBody(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg != null && !(arg instanceof ServletRequest) && !(arg instanceof ServletResponse)) {
                    return objectMapper.writeValueAsString(arg);
                }
            }
            return "";
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

}
