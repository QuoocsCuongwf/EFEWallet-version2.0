package com.QuoocsCuongwf.EFEWallet.WalletService.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DuplicateTransactionException extends RuntimeException{
    private final int code=2222;
    public DuplicateTransactionException (){
        super("Duplicate Transaction");
    }

    public DuplicateTransactionException (String message){
        super(message);
    }
}
