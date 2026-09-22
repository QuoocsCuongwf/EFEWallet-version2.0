package com.QuoocCuongwf.EFEWallet.AuthService.payload.dto;

import com.QuoocCuongwf.EFEWallet.AuthService.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDto {
    private String walletAddress;
    private WalletStatus status;
    private BigDecimal balance;
    private LocalDateTime createAt;
}
