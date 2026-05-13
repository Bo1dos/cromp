package com.cromp.iam.infrastructure.web.filter;

import com.cromp.iam.infrastructure.security.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(JwtConfig jwtConfig) {
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                Long userId = Long.parseLong(claims.getSubject());
                Object orgClaim = claims.get("org_id");
                String orgValue = orgClaim != null ? String.valueOf(orgClaim) : null;
                Long orgId = orgValue != null && !orgValue.isBlank() ? Long.parseLong(orgValue) : null;
                List<GrantedAuthority> authorities = new ArrayList<>();
                Object permissions = claims.get("permissions");
                if (permissions instanceof Collection<?> values) {
                    values.forEach(p -> authorities.add(new SimpleGrantedAuthority(String.valueOf(p))));
                }

                // Устанавливаем аутентификацию
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Сохраняем organizationId в атрибуты запроса для OrganizationContextFilter
                if (orgId != null) {
                    request.setAttribute("organizationId", orgId);
                }
            } catch (Exception e) {
                // Токен невалидный, просто не устанавливаем аутентификацию
                logger.debug("JWT parsing failed: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
