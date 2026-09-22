package com.QuoocCuongwf.EFEWallet.AuthService.payload.dto;

import java.io.Serializable;

public record PendingRegister(
        String email,
        String password,
        String firstName,
        String lastName
) implements Serializable {
}