package com.cromp.iam.infrastructure.security;

import com.cromp.iam.application.port.TokenIssuerPort;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.infrastructure.security.config.JwtConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

@Component
public class JwtTokenIssuer implements TokenIssuerPort {

    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;

    public JwtTokenIssuer(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueToken(User user, Organization activeOrganization, Collection<String> permissions) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpirationMs());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("org_id", activeOrganization != null ? activeOrganization.getId().toString() : "")
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}