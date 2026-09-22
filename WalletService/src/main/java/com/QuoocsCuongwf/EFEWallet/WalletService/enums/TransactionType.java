package com.QuoocsCuongwf.EFEWallet.WalletService.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    // Money coming into the wallet
    DEPOSIT("Funds added via external source (Bank/MoMo)"),
    RECEIVE("Funds received from another user"),
    REFUND("Funds returned from a failed or cancelled payment"),

    // Money going out of the wallet
    WITHDRAW("Funds transferred to external bank account"),
    TRANSFER("Funds sent to another user"),
    PAYMENT("Payment for services or products"),

    // System adjustments
    ADJUSTMENT("System-level balance correction");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    /**
     * Determines if the transaction type increases or decreases the balance.
     * @return true if it's a credit (increase), false if it's a debit (decrease).
     */
    public boolean isCredit() {
        return this == DEPOSIT || this == RECEIVE || this == REFUND;
    }
}