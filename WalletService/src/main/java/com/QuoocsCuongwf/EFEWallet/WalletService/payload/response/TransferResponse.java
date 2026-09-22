package com.QuoocsCuongwf.EFEWallet.WalletService.payload.response;

import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import lombok.*;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {

    private UUID transactionId;
    private TransactionStatus status;
    private BigDecimal amount;
    private UUID fromWalletId;
    private UUID toWalletId;
    private LocalDateTime createdAt;
}