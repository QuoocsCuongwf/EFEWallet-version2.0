package com.QuoocsCuongwf.EFEWallet.WalletService.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
