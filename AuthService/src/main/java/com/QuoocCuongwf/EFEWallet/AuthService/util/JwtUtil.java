package com.QuoocCuongwf.EFEWallet.AuthService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
