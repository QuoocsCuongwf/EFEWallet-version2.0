package com.QuoocCuongwf.EFEWallet.AuthService.enums;

public enum Role {
    USER,
    ADMIN;
    public String getAuthority() {
        return "ROLE_"+this.name();
    }
}
