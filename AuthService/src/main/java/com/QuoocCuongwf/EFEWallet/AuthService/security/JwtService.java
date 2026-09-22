package com.QuoocCuongwf.EFEWallet.AuthService.security;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.service.UserService;
import com.QuoocCuongwf.EFEWallet.AuthService.util.JwtUtil;
import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Data
public class JwtService {
    @Autowired
    private final RedisTemplate<Object,Object> redisTemplate;
    private final UserService userService;

    @Value("${jwt.secret:${JWT_SECRET:default_secret_key_very_long_for_security}}")
    private String secretKey;
    @Value("${jwt.expiration-ms:${JWT_EXPIRATION_MS:3600000}}")
    private long expirationMs;
    @Autowired
    private UserReponsitory userReponsitory;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(CustomUserDetails customUserDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        List<String> permissions = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        List<String> roles = customUserDetails.getUser().getRole().stream()
                .map(role -> role.getName())
                .toList();

        return Jwts.builder()
                .setSubject(customUserDetails.getId().toString())
                .claim("roles", roles)           // optional
                .claim("permissions", permissions)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails customUserDetails) {
        Date now = new Date();

        long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L;

        Date exp = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE);
        String refeshToken=Jwts.builder()
                .setSubject(customUserDetails.getId().toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSignInKey())
                .compact();
        redisTemplate.opsForValue().set(customUserDetails.getId().toString(),refeshToken,7, TimeUnit.DAYS);
        return refeshToken;
    }
    public String refreshToken(String refreshToken){
        Claims claims= parseToken(refreshToken);
        UUID userId=getUserIdFromJWT(refreshToken);
        String stored=(String) redisTemplate.opsForValue().get(userId.toString());
        if (!refreshToken.equals(stored)) {
            throw new JwtException("Token bị reuse / hack");
        }
        CustomUserDetails customUserDetails =
                (CustomUserDetails) userService.loadUserById(userId);

        return generateToken(customUserDetails);
    }
    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public User getCurrrentUser(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
           throw new RuntimeException("Get current user faild");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Get current user faild");
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
    public Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey().toString())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }
    public void addBlackListToken(String token){
        redisTemplate.opsForValue().set("auth:blacklist:"+token,1,expirationMs,TimeUnit.MILLISECONDS);
    }
    public boolean isBlackList(String token){
        String key="auth:blacklist:"+token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
