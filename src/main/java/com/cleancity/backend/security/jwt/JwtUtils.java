package com.cleancity.backend.security.jwt;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.domain.RoleType;
import com.cleancity.backend.auth.security.AccountDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final int MIN_SECRET_BYTES = 32;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:900000}")
    private int jwtExpirationMs;

    private Key signingKey;

    @PostConstruct
    void initSigningKey() {
        this.signingKey = resolveKey(jwtSecret);
    }

    public String generateJwtToken(Authentication authentication) {
        AccountDetailsImpl accountDetails = (AccountDetailsImpl) authentication.getPrincipal();
        List<String> roles = accountDetails.getRoles().stream()
                .map(RoleType::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(accountDetails.getEmail())
                .claim("accountId", accountDetails.getAccountId().toString())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenFromAccount(Account account) {
        List<String> roles = account.getRoles().stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(account.getEmail())
                .claim("accountId", account.getId().toString())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Key resolveKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is required and must be at least 32 bytes");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes (256 bits). Generate with: openssl rand -base64 32");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parse(authToken);
            return true;
        } catch (MalformedJwtException | IllegalArgumentException | SignatureException
                 | ExpiredJwtException | UnsupportedJwtException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }
}
