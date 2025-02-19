package com.example.stockmarketsimulator.modules.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
@Slf4j
public class JwtUtil {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username) {
        log.info("🔑 Generating token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();

        // Fetch user details from SecurityContext or UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Ensure authorities are not null
        List<String> roles = userDetails.getAuthorities() == null ?
                new ArrayList<>() : userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("✅ Extracted roles for token: {}", roles); // Debugging log

        claims.put("roles", roles); // Add roles to JWT
        log.info("Roles added to token: {}", roles);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("✅ Token generated successfully for user: {}", username);
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.info("✅ Token validation successful.");
            return true;
        } catch (JwtException e) {
            log.error("❌ Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.info("✅ Extracted username: {}", username);
            return username;
        } catch (Exception e) {
            log.error("❌ Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object rolesObj = claims.get("roles"); // Debugging step
            log.info("✅ Extracted raw roles from token: {}", rolesObj);

            if (rolesObj == null) {
                log.warn("⚠️ No roles found in token!");
                return Collections.emptyList();
            }

            List<String> roles = claims.get("roles", List.class);
            log.info("✅ Extracted roles: {}", roles);
            return roles;
        } catch (Exception e) {
            log.error("❌ Failed to extract roles from token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}
