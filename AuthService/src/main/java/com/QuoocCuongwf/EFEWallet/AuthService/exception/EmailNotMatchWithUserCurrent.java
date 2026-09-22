package com.QuoocCuongwf.EFEWallet.AuthService.exception;

import java.util.UUID;

public class EmailNotMatchWithUserCurrent extends RuntimeException {
    public EmailNotMatchWithUserCurrent(UUID userId, String email) {
        super(userId + " not use "+ email);
    }
}
