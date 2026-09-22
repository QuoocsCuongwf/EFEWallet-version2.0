package com.QuoocsCuongwf.EFEWallet.WalletService.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NotBlank
    private String toWalletAddress;

    @NotNull
    @DecimalMin(value = "0.0001", inclusive = true)
    private BigDecimal amount;

    private String description;
    private String referenceCode;
}
