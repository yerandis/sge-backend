package com.yerandis.sge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", source.getCorsConfigurations().isEmpty()
                ? new UrlBasedCorsConfigurationSource().getCorsConfigurations().getOrDefault("/**", config)
                : config);

        // WebSocket — SockJS hace peticiones HTTP a /ws/info, /ws/iframe, etc.
        // antes de establecer la conexión WebSocket real
//        source.registerCorsConfiguration("/ws/**", config);

        // Forma simple y correcta:
        UrlBasedCorsConfigurationSource finalSource = new UrlBasedCorsConfigurationSource();
        finalSource.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(finalSource);
    }
}

//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
///**
// * Configura CORS globalmente para toda la API.
// *
// * CORS (Cross-Origin Resource Sharing): mecanismo de seguridad del navegador
// * que bloquea peticiones HTTP a un dominio diferente al que sirvió la página.
// *
// * El frontend corre en http://localhost:5173 (Vite).
// * El backend corre en http://localhost:8080 (Spring Boot).
// * Son orígenes diferentes → el navegador bloquea las peticiones → necesitamos CORS.
// *
// * En producción: cambia el allowedOrigin al dominio real del frontend.
// */
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//
//        // Orígenes permitidos
//        config.addAllowedOrigin("http://localhost:5173");  // Vite dev server
//        config.addAllowedOrigin("http://localhost:3000");  // Por si acaso
//
//        // Métodos HTTP permitidos
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("OPTIONS");
//
//        // Headers permitidos en las peticiones
//        config.addAllowedHeader("*");
//
//        // Permitir cookies y headers de autorización (para JWT futuro)
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/**", config);
//
//        return new CorsFilter(source);
//    }
//}
