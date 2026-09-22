package com.QuoocsCuongwf.EFEWallet.WalletService.service;

import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.VerifyTokenRequest;
import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.TransactionEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.TransactionNotFoundException;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.KafkaMessage.TransferMessage;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.request.TransferRequest;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.PageResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.TransactionDetailResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.TransactionHistoryItemResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.TransferResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.util.SecurityUtils;
import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.*;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.WalletGrpcClient;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepnsitory;
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WalletGrpcClient grpcClient;
    @Value("${app.security.internal-secret-key}")
    private String internalSecretKey;

    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey) {
        String rawData=SecurityUtils.getCurrentUserId()+request.getToWalletAddress()+request.getAmount();
        String hashValue = Hashing.hmacSha256(internalSecretKey.getBytes(StandardCharsets.UTF_8))
                .hashString(rawData, StandardCharsets.UTF_8)
                .toString();
        VerifyTokenRequest verifyTokenRequest=VerifyTokenRequest.newBuilder()
                .setToken(hashValue)
                .setUserId(SecurityUtils.getCurrentUserId().toString())
                .build();
        if (!grpcClient.verifyToken(verifyTokenRequest).getIsValid()){
            throw new RuntimeException("Verify not successfull");
        }
        UUID fromUserId = SecurityUtils.getCurrentUserId();
        WalletEntity toWallet = walletRepository.findByWalletAddressForUpdate(request.getToWalletAddress(), WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        UUID toUserId = toWallet.getUserId();

        WalletEntity fromWallet = walletRepository.findByUserIdForUpdate(fromUserId,WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Error Wallet tranfer not activate"));

        TransactionEntity transaction = TransactionEntity.builder()
                .fromWallet(fromWallet.getWalletAddress())
                .fromUserId(fromUserId)
                .toWallet(toWallet.getWalletAddress())
                .toUserId(toUserId)
                .idempotencyKey(idempotencyKey)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transaction = transactionRepnsitory.save(transaction);

        try {

            Boolean isDedit = walletService.debit(
                    request.getAmount(),
                    fromUserId
            );
            Boolean isCredit = walletService.credit(
                    request.getAmount(),
                    request.getToWalletAddress()
            );
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepnsitory.save(transaction);
            TransferMessage transferMessage = TransferMessage.builder()
                    .walletSend(transaction.getFromWallet())
                    .walletRecive(transaction.getFromWallet())
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .time(transaction.getUpdatedAt())
                    .transactionId(transaction.getId())
                    .build();
            kafkaTemplate.send("wallet-transactions",transferMessage);
            return TransferResponse.builder()
                    .transactionId(transaction.getId())
                    .status(TransactionStatus.SUCCESS)
                    .amount(request.getAmount())
                    .fromWalletId(fromUserId)
                    .toWalletId(toUserId)
                    .createdAt(transaction.getCreatedAt())
                    .build();

        } catch (Exception e) {

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepnsitory.save(transaction);

            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionHistoryItemResponse> getHistory(
            UUID userId,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable
    ) {
        Page<TransactionHistoryItemResponse> page = transactionRepnsitory
                .findHistoryByUserId(userId, status, type, pageable)
                .map(tx -> toHistoryItem(tx, userId));
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public TransactionDetailResponse getDetail(UUID userId, UUID transactionId) {
        TransactionEntity transaction = transactionRepnsitory
                .findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        return toDetail(transaction, userId);
    }

    private TransactionHistoryItemResponse toHistoryItem(TransactionEntity tx, UUID userId) {
        boolean outgoing = userId.equals(tx.getFromUserId());
        return TransactionHistoryItemResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .fee(tx.getFee())
                .currency(tx.getCurrency())
                .description(tx.getDescription())
                .fromWallet(tx.getFromWallet())
                .toWallet(tx.getToWallet())
                .direction(outgoing ? "OUT" : "IN")
                .counterpartyWallet(outgoing ? tx.getToWallet() : tx.getFromWallet())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }

    private TransactionDetailResponse toDetail(TransactionEntity tx, UUID userId) {
        boolean outgoing = userId.equals(tx.getFromUserId());
        return TransactionDetailResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .fee(tx.getFee())
                .currency(tx.getCurrency())
                .description(tx.getDescription())
                .fromWallet(tx.getFromWallet())
                .toWallet(tx.getToWallet())
                .fromUserId(tx.getFromUserId())
                .toUserId(tx.getToUserId())
                .direction(outgoing ? "OUT" : "IN")
                .counterpartyWallet(outgoing ? tx.getToWallet() : tx.getFromWallet())
                .externalTransactionId(tx.getExternalTransactionId())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }

}
