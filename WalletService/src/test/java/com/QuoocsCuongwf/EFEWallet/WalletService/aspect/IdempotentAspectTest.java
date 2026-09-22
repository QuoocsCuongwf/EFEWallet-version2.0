package com.QuoocsCuongwf.EFEWallet.WalletService.aspect;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.IdempotencyKeyRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.IdempotencyKey;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.IdempotencyStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.IdempotencyException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotentAspectTest {
    private IdempotencyKeyRepository idempotencyKeyRepository;
    private ObjectMapper objectMapper;
    private IdempotentAspect idempotentAspect;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository = mock(IdempotencyKeyRepository.class);
        objectMapper = mock(ObjectMapper.class);
        idempotentAspect = new IdempotentAspect(idempotencyKeyRepository, objectMapper);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowWhenIdempotencyKeyMissing() {
        setRequestHeader(null);
        setAuthenticatedUser("user-1");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        InvalidAlgorithmParameterException exception = assertThrows(
                InvalidAlgorithmParameterException.class,
                () -> idempotentAspect.HandlIdemponcy(joinPoint)
        );

        assertEquals("Missing idemponcy key", exception.getMessage());
    }

    @Test
    void shouldReturnCachedResponseWhenExistingSuccessWithSamePayload() throws Throwable {
        String idemKey = "idem-1";
        String userId = "user-1";
        String payloadJson = "{\"amount\":10}";
        String payloadHash = DigestUtils.sha256Hex(payloadJson);
        String cachedResponseBody = "{\"ok\":true,\"message\":\"cached\"}";

        setRequestHeader(idemKey);
        setAuthenticatedUser(userId);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object payload = new Object();
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);

        IdempotencyKey existing = IdempotencyKey.builder()
                .idempotencyKey(idemKey)
                .userId(userId)
                .requestHash(payloadHash)
                .status(IdempotencyStatus.SUCCESS)
                .responseBody(cachedResponseBody)
                .responseStatus(200)
                .build();

        when(idempotencyKeyRepository.findForUpdate(idemKey, userId)).thenReturn(Optional.of(existing));
        when(objectMapper.readValue(cachedResponseBody, Map.class)).thenReturn(Map.of("ok", true, "message", "cached"));

        Object result = idempotentAspect.HandlIdemponcy(joinPoint);

        ResponseEntity<?> response = assertInstanceOf(ResponseEntity.class, result);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        verify(joinPoint, times(0)).proceed();
    }

    @Test
    void shouldPersistProcessingAndSuccessForNewRequest() throws Throwable {
        String idemKey = "idem-2";
        String userId = "user-2";
        String payloadJson = "{\"amount\":100}";
        List<IdempotencyKey> savedSnapshots = new ArrayList<>();

        setRequestHeader(idemKey);
        setAuthenticatedUser(userId);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object payload = new Object();
        ResponseEntity<Map<String, Object>> controllerResponse = ResponseEntity.ok(Map.of("ok", true));

        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(joinPoint.proceed()).thenReturn(controllerResponse);
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        when(objectMapper.writeValueAsString(controllerResponse)).thenReturn("{\"status\":200}");
        when(idempotencyKeyRepository.findForUpdate(idemKey, userId)).thenReturn(Optional.empty());
        when(idempotencyKeyRepository.save(any(IdempotencyKey.class))).thenAnswer(invocation -> {
            IdempotencyKey source = invocation.getArgument(0);
            savedSnapshots.add(IdempotencyKey.builder()
                    .idempotencyKey(source.getIdempotencyKey())
                    .userId(source.getUserId())
                    .requestHash(source.getRequestHash())
                    .status(source.getStatus())
                    .responseBody(source.getResponseBody())
                    .responseStatus(source.getResponseStatus())
                    .build());
            return source;
        });

        Object result = idempotentAspect.HandlIdemponcy(joinPoint);

        assertEquals(controllerResponse, result);

        verify(idempotencyKeyRepository, times(2)).save(any(IdempotencyKey.class));
        assertEquals(IdempotencyStatus.PROCESSING, savedSnapshots.get(0).getStatus());
        assertEquals(IdempotencyStatus.SUCCESS, savedSnapshots.get(1).getStatus());
        assertEquals(idemKey, savedSnapshots.get(1).getIdempotencyKey());
        assertEquals(userId, savedSnapshots.get(1).getUserId());
        assertEquals(DigestUtils.sha256Hex(payloadJson), savedSnapshots.get(1).getRequestHash());
    }

    @Test
    void shouldThrowOnPayloadMismatchForExistingKey() throws Throwable {
        String idemKey = "idem-3";
        String userId = "user-3";
        String payloadJson = "{\"amount\":10}";
        String differentHash = DigestUtils.sha256Hex("{\"amount\":999}");

        setRequestHeader(idemKey);
        setAuthenticatedUser(userId);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object payload = new Object();
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);

        IdempotencyKey existing = IdempotencyKey.builder()
                .idempotencyKey(idemKey)
                .userId(userId)
                .requestHash(differentHash)
                .status(IdempotencyStatus.SUCCESS)
                .responseBody("{\"ok\":true}")
                .build();

        when(idempotencyKeyRepository.findForUpdate(idemKey, userId)).thenReturn(Optional.of(existing));

        IdempotencyException exception = assertThrows(
                IdempotencyException.class,
                () -> idempotentAspect.HandlIdemponcy(joinPoint)
        );

        assertEquals("Payload mismatch for existing idempotency key", exception.getMessage());
        verify(idempotencyKeyRepository, times(0)).save(any(IdempotencyKey.class));
        verify(joinPoint, times(0)).proceed();
    }

    private void setRequestHeader(String keyValue) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq("Idempotency-Key"))).thenReturn(keyValue);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setAuthenticatedUser(String userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
