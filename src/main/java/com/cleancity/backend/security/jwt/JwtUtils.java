package com.cleancity.backend.security.jwt;

import com.cleancity.backend.security.services.UserDetailsImpl;
import com.cleancity.backend.repository.UserRepository;
import com.cleancity.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    @Value("${app.jwtSecret:4f4d2f8016467389a9f4c3ecf52d5b62b083b4b60098f489f6b98ea6fbd4b6dc}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:9000000}")
    private int jwtExpirationMs;

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject((userPrincipal.getEmail()))
        .claim("roles", userPrincipal.getRole())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
    }
    
    public String generateTokenFromEmail(String email) {
        // try to add role claim if user exists
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                return Jwts.builder().setSubject(email).claim("roles", user.getRole()).setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(key(), SignatureAlgorithm.HS256)
                        .compact();
            }
        } catch (Exception e) {
            // fallback to token without role
        }
        return Jwts.builder().setSubject(email).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        try {
            // try base64 first (common configuration)
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (IllegalArgumentException e) {
            // fallback to using raw bytes of the secret (helps when secret is not base64-encoded)
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
        } catch (MalformedJwtException | IllegalArgumentException | SignatureException | ExpiredJwtException | UnsupportedJwtException e) {
            System.err.println("Invalid JWT Token: " + e.getMessage());
        }
        return false;
    }
}
