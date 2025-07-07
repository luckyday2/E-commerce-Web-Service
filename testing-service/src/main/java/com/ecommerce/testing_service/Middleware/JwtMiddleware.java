package com.ecommerce.testing_service.Middleware;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtMiddleware {
    private static final String SECRET = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=";

    // ✅ Validasi token (misal: format, signature, expired)
    public void validateToken(final String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token); // akan throw exception jika invalid
    }

    // ✅ Ambil username (subject) dari token
    public String getUsernameFromToken(final String token) {
        return getClaims(token).getSubject();

    }

    // ✅ Ambil role dari token
    public String getRoleFromToken(final String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("role");
    }

    // ✅ Ambil claims dari token
    public Claims getClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // Ambil kunci rahasia
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
