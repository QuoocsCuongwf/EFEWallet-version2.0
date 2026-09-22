package com.QuoocCuongwf.EFEWallet.AuthService.payload.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private final String typeToken="Bearer";
    private Long expiresIn;

    public LoginResponse(String token, String refreshToken, Long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
