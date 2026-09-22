package com.QuoocsCuongwf.EFEWallet.WalletService.controller;

import com.QuoocsCuongwf.EFEWallet.WalletService.annotation.Idempotent;
import com.QuoocsCuongwf.EFEWallet.WalletService.config.SecurityConstants;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import com.QuoocsCuongwf.EFEWallet.WalletService.service.TransactionService;
import com.QuoocsCuongwf.EFEWallet.WalletService.service.WalletService;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.*;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.request.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping(SecurityConstants.WALLET_API)
public class WalletController {
    private static final int MAX_PAGE_SIZE = 50;

    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> balance(
            Authentication auth
    ) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Get balance for user={}",  auth.getName());
        return ResponseEntity.ok(
                ApiResponse.success(walletService.balance(userId))
        );
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<WalletResponse>> wallet(
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Controller reached: by user={}", auth.getName());

        return ResponseEntity.ok(
                ApiResponse.success(walletService.wallet(userId))
        );
    }

    @PostMapping("/generation")
    public ResponseEntity<ApiResponse<WalletResponse>> generation(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Generate wallet for user={}", userId);
        return ResponseEntity.ok(
                ApiResponse.success(walletService.generation(userId))
        );
    }

    @PostMapping("/transfer")
    @Idempotent(
            type = TransactionType.TRANSFER,
            validatePayload = true
    )
    public ResponseEntity<ApiResponse<TransferResponse>> transfer (
            @RequestHeader("idempotency-Key") String idemKey,
            Authentication auth,
            @Valid @RequestBody TransferRequest transferRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.transfer(transferRequest, idemKey))
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionHistoryItemResponse>>> transactions(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType type
    ) {
        UUID userId = UUID.fromString(auth.getName());
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int pageNumber = Math.max(page, 0);
        log.info("Get transaction history for user={}, page={}, size={}", userId, pageNumber, pageSize);
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getHistory(
                        userId,
                        status,
                        type,
                        PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                ))
        );
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> transactionDetail(
            Authentication auth,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Get transaction detail id={} for user={}", id, userId);
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getDetail(userId, id))
        );
    }

}
