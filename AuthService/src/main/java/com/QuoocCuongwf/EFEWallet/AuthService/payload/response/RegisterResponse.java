package com.QuoocCuongwf.EFEWallet.AuthService.payload.response;


import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.WalletDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse<T> {
    private UUID userId;
    private String email;
    private String message;
    private T wallet;
}
