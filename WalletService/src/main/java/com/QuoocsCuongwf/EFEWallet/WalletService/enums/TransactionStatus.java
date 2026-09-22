package com.QuoocsCuongwf.EFEWallet.WalletService.enums;

public enum TransactionStatus {
    PENDING,   // Created, waiting for MoMo/Bank confirmation
    SUCCESS,   // Completed successfully
    FAILED,    // Rejected by provider or insufficient funds
    CANCELLED, // User cancelled the flow
    REVERSED   // Transaction was successful but later rolled back
}
