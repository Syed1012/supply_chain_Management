package com.cloud_based.supply_chain.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private String SECRET_KEY = "your_secret_key"; // Replace with a strong secret key
    private String REFRESH_SECRET_KEY = "your_refresh_secret_key"; // Strong refresh key

    // Generate Access Token with username, userId, and role
    public String generateToken(String username, String userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return createToken(claims, username, SECRET_KEY, 1000 * 60 * 60); // 1 hour access token
    }

    // Generate Refresh Token with username (refresh tokens typically have longer expiry)
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, REFRESH_SECRET_KEY, 1000 * 60 * 60 * 24 * 7); // 7 days refresh token
    }

    // Create JWT Token using the provided secret
    private String createToken(Map<String, Object> claims, String subject, String secret, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Expiry time
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // Extract username from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract userId from the token
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token, SECRET_KEY);
        return (String) claims.get("userId");
    }

    // Extract role from the token
    public Integer extractRole(String token) {
        Claims claims = extractAllClaims(token, SECRET_KEY);
        return (Integer) claims.get("role");
    }

    // Extract claims from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, SECRET_KEY);
        return claimsResolver.apply(claims);
    }

    // Extract all claims using the provided secret
    private Claims extractAllClaims(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // Validate access token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token, SECRET_KEY));
    }

    // Validate refresh token
    public Boolean validateRefreshToken(String token, String username) {
        final String extractedUsername = extractClaim(token, Claims::getSubject);
        return (extractedUsername.equals(username) && !isTokenExpired(token, REFRESH_SECRET_KEY));
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token, String secret) {
        return extractExpiration(token, secret).before(new Date());
    }

    // Extract expiration date from the token
    private Date extractExpiration(String token, String secret) {
        return extractAllClaims(token, secret).getExpiration();
    }

    // Refresh Access Token using the Refresh Token
    public String refreshAccessToken(String refreshToken) {
        final Claims claims = extractAllClaims(refreshToken, REFRESH_SECRET_KEY);
        String username = claims.getSubject();
        String userId = (String) claims.get("userId");
        Integer role = (Integer) claims.get("role");

        return generateToken(username, userId, role); // Generate new access token
    }
}
