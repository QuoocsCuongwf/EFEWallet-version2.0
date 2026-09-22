package com.QuoocsCuongwf.EFEWallet.WalletService.payload.response;

import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
    private UUID userId;
    private UUID walletId;
    private String walletAddress;
    private WalletStatus status;
    private BigDecimal balance;
    private LocalDateTime createAt;
}
