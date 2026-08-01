package com.yerandis.sge.dto.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de notificación: lo que viaja tanto por REST como por WebSocket.
 * El mismo objeto se serializa a JSON en ambos canales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private String entityType;
    private UUID entityId;
    private String triggeredBy;
    private boolean read;
    private LocalDateTime createdAt;

    /**
     * URL de navegación generada en el servidor.
     * Si la notificación es sobre un empleado con ID 5,
     * navigateTo = "/employees/5"
     * El frontend puede redirigir directamente.
     */
    private String navigateTo;
}
