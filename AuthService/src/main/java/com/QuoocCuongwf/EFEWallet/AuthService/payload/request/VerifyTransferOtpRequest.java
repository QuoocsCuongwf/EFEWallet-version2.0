package com.QuoocCuongwf.EFEWallet.AuthService.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyTransferOtpRequest {
    @NotBlank
    private String identifier;

    @NotBlank
    private String otpCode;

    @NotBlank
    private String toWalletAddress;

    @NotNull
    @DecimalMin(value = "0.0001", inclusive = true)
    private BigDecimal amount;

    public TransferRequest toTransferRequest() {
        return TransferRequest.builder()
                .toWalletAddress(toWalletAddress)
                .amount(amount)
                .build();
    }
}
