package com.yerandis.sge.config;

import com.yerandis.sge.repository.admin.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ApplicationConfig: beans de infraestructura de autenticación.
 *
 * ═══════════════════════════════════════════════════════════════
 * POR QUÉ ESTA CLASE EXISTE SEPARADA DE SecurityConfig
 * ═══════════════════════════════════════════════════════════════
 *
 * Separar la definición de UserDetailsService, PasswordEncoder y
 * AuthenticationProvider de SecurityConfig rompe el ciclo:
 *
 * Árbol de dependencias LIMPIO:
 *
 *   ApplicationConfig → UserRepository     (sin problemas)
 *   JwtService        → (valores de propiedades, sin beans)
 *   JwtAuthFilter     → JwtService         (sin ciclo)
 *   SecurityConfig    → JwtAuthFilter      (sin ciclo)
 *                    → AuthenticationProvider (de ApplicationConfig)
 *
 * 🔄 Comparación Java:
 * Es el patrón de separación de responsabilidades aplicado a la
 * configuración. Cada @Configuration tiene una responsabilidad única.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * UserDetailsService: cómo Spring Security carga un usuario por username.
     *
     * Se usa SOLO en dos momentos:
     * 1. Durante el login (AuthenticationManager verifica credenciales)
     * 2. Cuando necesitas el User completo (ej: para generar el JWT en AuthService)
     *
     * NO se usa en JwtAuthFilter (eso es lo que elimina el ciclo).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username
                ));
    }

    /**
     * PasswordEncoder: BCrypt para hashear contraseñas.
     *
     * BCrypt incluye sal automática. Nunca es posible revertir el hash.
     * Rounds = 10 por defecto (2^10 = 1024 iteraciones).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider: orquesta la autenticación por username/password.
     *
     * DaoAuthenticationProvider:
     * 1. Llama a userDetailsService.loadUserByUsername(username)
     * 2. Compara la contraseña con BCrypt
     * 3. Si coincide → autenticación exitosa
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager: el orquestador central de la autenticación.
     * AuthService lo usa para autenticar las credenciales del login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
