package com.magicworld.tfg_angular_springboot.configuration.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String getToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getAuthorities());
        return getToken(claims, user);
    }

    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60*2)) // 2 horas
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaim(token, Claims::getExpiration);
    }
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public String generateOAuth2PendingToken(String email, String firstname, String lastname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("firstname", firstname != null ? firstname : "");
        claims.put("lastname", lastname != null ? lastname : "");
        claims.put("type", "oauth2_pending");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 minutos
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseOAuth2PendingToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (!"oauth2_pending".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }
        return claims;
    }
}
