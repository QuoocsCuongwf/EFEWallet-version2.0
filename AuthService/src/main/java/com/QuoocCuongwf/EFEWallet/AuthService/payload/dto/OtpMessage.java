package com.QuoocCuongwf.EFEWallet.AuthService.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class OtpMessage {
    private String action;
    private String identifier;
    private String otp;
}
