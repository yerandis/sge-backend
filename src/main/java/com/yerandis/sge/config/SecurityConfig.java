package com.yerandis.sge.config;

import com.yerandis.sge.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig: configuración de la cadena de filtros de seguridad HTTP.
 *
 * ═══════════════════════════════════════════════════════════════
 * ÁRBOL DE DEPENDENCIAS (sin ciclos):
 * ═══════════════════════════════════════════════════════════════
 *
 *   SecurityConfig
 *     ├── JwtAuthFilter
 *     │     └── JwtService (solo lee propiedades)
 *     └── AuthenticationProvider (de ApplicationConfig)
 *           ├── UserDetailsService → UserRepository
 *           └── PasswordEncoder
 *
 * Ningún bean depende de otro que lo necesite para crearse.
 * ═══════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Activa @PreAuthorize en los métodos de servicio
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter       jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    // Rutas que no requieren autenticación
    private static final String[] PUBLIC_ROUTES = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/ws/**",
            "/api/v1/employees/**",
            "/api/v1/departments/**",
            "/api/v1/roles/**",
    };

    /**
     * ═══════════════════════════════════════════════════════════
     * IMPLEMENTACIÓN ACTIVA: JWT (Stateless)
     * ═══════════════════════════════════════════════════════════
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Deshabilitar CSRF: no necesario en APIs stateless con JWT
                // CSRF protege cookies de sesión; JWT en header Authorization no lo necesita
                .csrf(AbstractHttpConfigurer::disable)

                // CORS configurado en CorsConfig.java
                .cors(cors -> cors.configure(http))

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                        // Los permisos granulares se verifican con @PreAuthorize en cada método
                        // No es necesario configurarlos aquí route por route
                )

                // ══ JWT: política STATELESS ══════════════════════════
                // Spring NO crea sesiones HTTP. Cada petición debe traer el JWT.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)

                // Nuestro filtro JWT se ejecuta ANTES del filtro estándar de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        /*
         * ═══════════════════════════════════════════════════════
         * ALTERNATIVA: SESSION-BASED — COMENTADO
         * Para activar: comenta el bloque JWT y descomenta este.
         * Requiere también cambiar SessionCreationPolicy a IF_REQUIRED.
         * ═══════════════════════════════════════════════════════
         *
         * http
         *     .csrf(AbstractHttpConfigurer::disable)
         *     .cors(cors -> cors.configure(http))
         *     .authorizeHttpRequests(auth -> auth
         *         .requestMatchers(PUBLIC_ROUTES).permitAll()
         *         .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
         *         .anyRequest().authenticated()
         *     )
         *     .sessionManagement(session ->
         *         session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
         *               .maximumSessions(1)
         *     )
         *     .formLogin(form -> form
         *         .loginProcessingUrl("/api/v1/auth/login")
         *         .successHandler((req, res, auth) -> {
         *             res.setContentType("application/json");
         *             res.getWriter().write("{\"success\":true,\"message\":\"Login exitoso\"}");
         *         })
         *         .failureHandler((req, res, ex) -> {
         *             res.setStatus(401);
         *             res.setContentType("application/json");
         *             res.getWriter().write("{\"success\":false,\"message\":\"Credenciales inválidas\"}");
         *         })
         *     )
         *     .logout(logout -> logout
         *         .logoutUrl("/api/v1/auth/logout")
         *         .logoutSuccessHandler((req, res, auth) -> {
         *             res.setStatus(200);
         *             res.getWriter().write("{\"success\":true,\"message\":\"Sesión cerrada\"}");
         *         })
         *     )
         *     .authenticationProvider(authenticationProvider);
         */

        return http.build();
    }
}
