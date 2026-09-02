package com.yerandis.sge.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig: configura el broker de mensajes STOMP.
 *
 * STOMP (Simple Text Oriented Messaging Protocol):
 * Un protocolo de mensajería sobre WebSocket.
 * Define la estructura de los mensajes: destino, headers, body.
 *
 * El flujo de mensajes:
 *
 * Cliente → suscribe a /topic/notifications
 * Servidor → publica en /topic/notifications
 * Spring broker → distribuye el mensaje a todos los suscritos
 *
 * Para mensajes cliente → servidor:
 * Cliente → envía a /app/notifications/ping
 * Spring → rutea al @MessageMapping correspondiente
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /**
         * enableSimpleBroker("/topic", "/queue"):
         * Activa el broker interno de Spring.
         *
         * /topic → mensajes broadcast (un emisor, muchos receptores)
         * /queue → mensajes privados (un emisor, un receptor específico)
         *
         * En producción con múltiples servidores:
         * Usa RabbitMQ o ActiveMQ como broker externo:
         * config.enableStompBrokerRelay("/topic", "/queue")
         *       .setRelayHost("rabbitmq-server")
         *       .setRelayPort(61613);
         */
        config.enableSimpleBroker("/topic", "/queue");

        /**
         * setApplicationDestinationPrefixes("/app"):
         * Los mensajes del cliente con este prefijo van a @MessageMapping.
         * Ejemplo: cliente envía a "/app/ping" →
         *          Spring busca @MessageMapping("/ping")
         */
        config.setApplicationDestinationPrefixes("/app");

        /**
         * setUserDestinationPrefix("/user"):
         * Para mensajes privados: /user/{username}/queue/notifications
         * Solo llega al usuario específico.
         */
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /**
         * El endpoint WebSocket al que se conectan los clientes.
         * ws://localhost:8080/ws
         *
         * withSockJS(): fallback para navegadores sin soporte WebSocket.
         * SockJS simula WebSocket con long-polling u otros mecanismos.
         */
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:3000")
                .withSockJS();
    }
}


// WebSocketConfig.java — versión completa corregida

//package com.yerandis.sge.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic", "/queue");
//        config.setApplicationDestinationPrefixes("/app");
//        config.setUserDestinationPrefix("/user");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                // Usar setAllowedOriginPatterns en lugar de setAllowedOrigins
//                // cuando allowCredentials=true (requerimiento de Spring Security)
//                .setAllowedOriginPatterns(
//                        "http://localhost:5173",
//                        "http://localhost:3000",
//                        "http://localhost:*"    // cualquier puerto local durante desarrollo
//                )
//                .withSockJS();
//    }
//}
