package com.cleancity.backend.security.jwt;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.domain.RoleType;
import com.cleancity.backend.auth.repository.AccountRepository;
import com.cleancity.backend.auth.security.AccountDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:900000}")
    private int jwtExpirationMs;

    private final AccountRepository accountRepository;

    public JwtUtils(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
                .signWith(key(), SignatureAlgorithm.HS256)
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
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** @deprecated Use {@link #generateTokenFromAccount(Account)} */
    @Deprecated
    public String generateTokenFromEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(this::generateTokenFromAccount)
                .orElseGet(() -> Jwts.builder()
                        .setSubject(email)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                        .signWith(key(), SignatureAlgorithm.HS256)
                        .compact());
    }

    private Key key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (IllegalArgumentException e) {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException | IllegalArgumentException | SignatureException
                 | ExpiredJwtException | UnsupportedJwtException e) {
            System.err.println("Invalid JWT Token: " + e.getMessage());
        }
        return false;
    }
}
