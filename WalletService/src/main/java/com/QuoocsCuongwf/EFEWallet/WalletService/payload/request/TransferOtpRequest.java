package com.QuoocsCuongwf.EFEWallet.WalletService.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TransferOtpRequest {
    private String identifier;
    private String OptCode;
}
