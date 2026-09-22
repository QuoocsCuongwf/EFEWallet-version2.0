package com.QuoocsCuongwf.EFEWallet.WalletService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Component
public class JwtUtil {
    private final SecretKey sighingKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey){
        this.sighingKey=Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(sighingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
