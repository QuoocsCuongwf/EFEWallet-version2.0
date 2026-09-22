package com.QuoocsCuongwf.EFEWallet.WalletService.exception;

public class WalletIsExistedException extends RuntimeException {
    public WalletIsExistedException(String message) {
        super(message);
    }
}
