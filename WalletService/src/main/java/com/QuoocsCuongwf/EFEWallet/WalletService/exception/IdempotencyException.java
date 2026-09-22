package com.QuoocsCuongwf.EFEWallet.WalletService.exception;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String s) {
        super(s);
    }
}
