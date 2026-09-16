package com.yerandis.sge.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService: única responsabilidad — crear y verificar tokens JWT.
 *
 * NO depende de UserDetailsService ni de SecurityConfig.
 * Esto es lo que rompe el ciclo de dependencias.
 *
 * Dependencias:
 *   JwtService → (ningún bean de Spring Security)
 */
@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generación ────────────────────────────────────────────────

    /**
     * Genera el access token (24h).
     * Incluye los roles como claim adicional.
     */
//    public String generateAccessToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", userDetails.getAuthorities()
//                .stream()
//                .map(a -> a.getAuthority())
//                .toList());
//        return buildToken(claims, userDetails.getUsername(), expirationMs);
//    }

//    public String generateAccessToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("permissions", userDetails.getAuthorities()  // ← renombrar a "permissions"
//                .stream()
//                .map(a -> a.getAuthority())
//                .toList());
//        return buildToken(claims, userDetails.getUsername(), expirationMs);
//    }
    public String generateAccessToken(UserDetails userDetails) {

        List<String> permissions = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        System.out.println("========== GENERANDO JWT ==========");
        System.out.println("USER: " + userDetails.getUsername());
        System.out.println("PERMISSIONS: " + permissions);
        System.out.println("===================================");

        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", permissions);

        return buildToken(
                claims,
                userDetails.getUsername(),
                expirationMs
        );
    }

//    public List<String> extractPermissions(String token) {
//        Object perms = extractAllClaims(token).get("permissions");  // ← leer "permissions"
//        if (perms instanceof List<?> list) {
//            return (List<String>) list;
//        }
//        return List.of();
//    }
public List<String> extractPermissions(String token) {

    Object perms = extractAllClaims(token).get("permissions");

    List<String> permissions;

    if (perms instanceof List<?> list) {
        permissions = list.stream()
                .map(String::valueOf)
                .toList();
    } else {
        permissions = List.of();
    }

    System.out.println("========== JWT LEÍDO ==========");
    System.out.println("PERMISSIONS: " + permissions);
    System.out.println("===============================");

    return permissions;
}

    /**
     * Genera el refresh token (7 días).
     * Solo contiene el subject (username). Sin roles ni datos extra.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Extracción ────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
//    public java.util.List<String> extractRoles(String token) {
//        Object roles = extractAllClaims(token).get("roles");
//        if (roles instanceof java.util.List<?> list) {
//            return (java.util.List<String>) list;
//        }
//        return java.util.List.of();
//    }

    // ── Validación ────────────────────────────────────────────────

    /**
     * Valida el token: firma correcta + no expirado.
     *
     * IMPORTANTE: esta versión NO consulta la base de datos.
     * La validación es puramente criptográfica.
     * Esto elimina la necesidad de UserDetailsService en el filtro.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // lanza excepción si la firma es inválida o expiró
            return true;
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Versión con verificación adicional del username.
     * Usar cuando tienes el UserDetails disponible (ej: en AuthService).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) &&
                    !extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
