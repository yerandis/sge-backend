package com.yerandis.sge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthFilter: intercepta cada petición HTTP y verifica el JWT.
 *
 * ═══════════════════════════════════════════════════════════════
 * SOLUCIÓN AL CICLO DE DEPENDENCIAS
 * ═══════════════════════════════════════════════════════════════
 *
 * ANTES (❌ ciclo):
 *   JwtAuthFilter inyectaba UserDetailsService
 *   UserDetailsService estaba definido en SecurityConfig
 *   SecurityConfig inyectaba JwtAuthFilter
 *   → Spring no podía resolver el orden de creación
 *
 * AHORA (✅ sin ciclo):
 *   JwtAuthFilter solo inyecta JwtService
 *   JwtService no depende de ningún bean de Security
 *   SecurityConfig inyecta JwtAuthFilter sin problemas
 *
 * ¿Por qué NO necesitamos UserDetailsService aquí?
 * Porque el JWT ya contiene el username y los roles.
 * En lugar de hacer una query a la BD para reconstruir el usuario,
 * leemos esa información directamente del token.
 *
 * Dependencias del filtro:
 *   JwtAuthFilter → JwtService (solo)
 *
 * Flujo:
 *   1. Extrae el token del header Authorization
 *   2. Verifica la firma con JwtService (sin BD)
 *   3. Extrae username y roles del payload del token
 *   4. Crea el objeto de autenticación con esos datos
 *   5. Lo registra en el SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    // ← Solo JwtService. Sin UserDetailsService. Sin UserRepository.

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay Bearer token, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // Verificar firma y expiración (sin consultar la BD)
            if (!jwtService.isTokenValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Solo autenticar si no hay autenticación previa en este contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // Extraer datos directamente del payload del JWT
                String          username    = jwtService.extractUsername(jwt);
                List<String>    permissions = jwtService.extractPermissions(jwt);
//                List<String> roles = jwtService.extractRoles(jwt);

                // Convertir los roles del JWT a GrantedAuthority
                List<SimpleGrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Crear el objeto de autenticación
                // El "principal" es el username (String), no el objeto User
                // No necesitamos cargar el User completo para cada petición
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,        // credentials: null (ya autenticado)
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Registrar en el SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            log.error("Error procesando JWT: {}", e.getMessage());
            // No lanzamos la excepción: la petición llegará sin autenticación
            // Spring Security la rechazará con 401 si la ruta está protegida
        }

        filterChain.doFilter(request, response);
    }
}
