package com.QuoocCuongwf.EFEWallet.AuthService.config;

public final class SecurityConstants {

    private SecurityConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String API_ROOT="/api/v1";
    public static final String AUTH_API = API_ROOT + "/auth";
    public static final String WALLET_API = API_ROOT + "/wallet";
    public static final String ADMIN_API =API_ROOT + "/admin";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final int BCRYPT_STRENGTH = 12;

    public static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    public static final String ACTION_REG = "REGISTER";
    public static final String ACTION_CHANGE_PASSWORD = "CHANGE PASSWORD";
    public static final String ACTION_TRANSFER = "TRANSFER";
    public static final String ACTION_CHANGE_INFO = "CHANGE INFO";

}
