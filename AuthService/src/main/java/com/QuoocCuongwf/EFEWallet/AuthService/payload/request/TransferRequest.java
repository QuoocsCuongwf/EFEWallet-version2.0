package com.QuoocCuongwf.EFEWallet.AuthService.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
