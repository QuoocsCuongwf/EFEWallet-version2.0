package com.QuoocCuongwf.EFEWallet.AuthService.payload.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshResponse {
     private String accessToken;
     private final String tokenType="Bearer";
}
