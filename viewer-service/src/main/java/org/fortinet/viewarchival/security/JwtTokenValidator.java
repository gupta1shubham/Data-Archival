package org.fortinet.viewarchival.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTokenValidator {

    public JwtTokenValidator() {
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .build()
                .parseClaimsJwt(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .build()
            .parseClaimsJwt(token)
            .getBody();

        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .build()
            .parseClaimsJwt(token)
            .getBody();

        return (List<String>) claims.get("roles");
    }
}