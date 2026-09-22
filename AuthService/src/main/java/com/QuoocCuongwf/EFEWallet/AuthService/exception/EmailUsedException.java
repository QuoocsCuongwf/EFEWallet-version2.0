package com.QuoocCuongwf.EFEWallet.AuthService.exception;

public class EmailUsedException extends RuntimeException {
    public EmailUsedException(String email) {
        super(email + " Used to register other wallet");
    }
}
