package com.QuoocsCuongwf.EFEWallet.WalletService.annotation;

import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    TransactionType type();
    boolean validatePayload() default true;
}
