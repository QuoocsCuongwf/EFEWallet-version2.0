package com.QuoocCuongwf.EFEWallet.AuthService.exception;

public class RolesNotFoundException extends RuntimeException{

    public RolesNotFoundException(String role){
        super("Error: " + role + " Not found");
    }
}
