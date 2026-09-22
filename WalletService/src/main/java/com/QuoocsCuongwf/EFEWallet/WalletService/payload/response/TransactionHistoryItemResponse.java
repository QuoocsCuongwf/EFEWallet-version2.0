package com.QuoocsCuongwf.EFEWallet.WalletService.payload.response;

import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryItemResponse {
    private UUID id;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private BigDecimal fee;
    private String currency;
    private String description;
    private String fromWallet;
    private String toWallet;
    /** OUT if current user sent; IN if received */
    private String direction;
    private String counterpartyWallet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
