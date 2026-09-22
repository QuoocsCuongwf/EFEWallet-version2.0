package com.QuoocCuongwf.EFEWallet.AuthService.exception;

public class EmailNotExistException extends RuntimeException {
    public EmailNotExistException(String email) {
        super(email + " not exist");
    }
}
