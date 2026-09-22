package com.QuoocsCuongwf.EFEWallet.WalletService.security;

import com.QuoocsCuongwf.EFEWallet.WalletService.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        log.info("Authorization header: {}", request.getHeader("Authorization"));
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                log.info(token);
                Claims claims = jwtUtil.parseToken(token);
                String userId = claims.getSubject();
                List<String> roles = claims.get("role",List.class);
                List<String> permissions = claims.get("permissions", List.class);
                List<GrantedAuthority> authorities=new ArrayList<>();
                if (roles != null) {
                    roles.forEach(r ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + r))
                    );
                }
                if (permissions != null) {
                    permissions.forEach(p ->
                            authorities.add(new SimpleGrantedAuthority(p))
                    );
                }
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities);
                log.info("Authenticated user: {}", userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                // Invalid token — don't set authentication, let Spring Security reject it
            }
        }

        log.info("Before filterChain: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
        log.info("After filterChain");
    }
}

