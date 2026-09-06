package com.yerandis.sge.service.serviceImpl.notification;

import com.yerandis.sge.dto.dto.notification.NotificationDto;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.entity.notification.Notification;
import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.repository.notification.NotificationRepository;
import com.yerandis.sge.service.serviceInterface.notification.NotificationAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NotificationService: el corazón del sistema de notificaciones.
 *
 * Responsabilidades:
 * 1. Persistir notificaciones en BD (histórico REST)
 * 2. Publicar notificaciones por WebSocket (tiempo real)
 * 3. Gestionar el estado de lectura
 *
 * SimpMessagingTemplate: el "publicador" de WebSocket de Spring.
 * Permite enviar mensajes a destinos STOMP desde cualquier clase.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements NotificationAppService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Canal WebSocket al que se suscriben todos los clientes.
     * /topic/notifications → broadcast a TODOS los suscritos
     *
     * Alternativa para notificaciones privadas:
     * /user/{username}/queue/notifications → solo a ese usuario
     */
    private static final String WS_DESTINATION = "/topic/notifications";

    // ── Publicación de eventos ────────────────────────────────────

    /**
     * Crea y publica una notificación de empleado.
     * Se llama desde EmployeeService en cada operación CRUD.
     */
    @Transactional
    @Override
    public void publishEvent(NotificationType type, String title, String employeeName, UUID employeeId, String entityType) {

        String triggeredBy = getCurrentUsername();
//        String title;
        String message;

        switch (type) {
            case CREATED -> {
//                title = "Nuevo empleado registrado";
                message = String.format("%s ha sido añadido al sistema por %s",
                        employeeName, triggeredBy);
            }
            case UPDATED -> {
//                title = "Empleado actualizado";
                message = String.format("Los datos de %s han sido actualizados por %s",
                        employeeName, triggeredBy);
            }
            case DELETED -> {
//                title = "Empleado eliminado";
                message = String.format("%s ha sido eliminado del sistema por %s",
                        employeeName, triggeredBy);
            }
            default -> {
//                title = "Evento de empleado";
                message = employeeName;
            }
        }

        publish(type, title, message, entityType, employeeId, triggeredBy);
    }

    /**
     * Método genérico de publicación.
     * Persiste en BD y envía por WebSocket.
     */
    @Transactional
    @Override
    public void publish(NotificationType type, String title, String message, String entityType, UUID entityId, String triggeredBy) {

        // 1. Persistir en base de datos
        Notification notification = Notification.builder()
//                .id(UUID.randomUUID())
                .type(type)
                .title(title)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .triggeredBy(triggeredBy)
                .build();


        System.out.println(">>>>>  notification title: " + notification.getTitle());
        Notification notificationSaved = notificationRepository.save(notification);
        System.out.println(">>>>>  notification id: " + notificationSaved.getId());

        // 2. Convertir a DTO
        NotificationDto dto = toDto(notificationSaved);

        // 3. Publicar por WebSocket a todos los clientes suscritos
        try {
            messagingTemplate.convertAndSend(WS_DESTINATION, dto);
            log.debug("Notificación enviada por WS: {} → {}", WS_DESTINATION, title);
        } catch (Exception e) {
            // Si WebSocket falla, la notificación ya está en BD.
            // Los clientes la verán al consultar el historial REST.
            log.warn("Error enviando notificación por WebSocket: {}", e.getMessage());
        }
    }

    // ── Consultas REST ────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public PageResponse<NotificationDto> getAll(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new PageResponse<>(result.map(this::toDto));
    }

    @Transactional(readOnly = true)
    @Override
    public long countUnread() {
        return notificationRepository.countByReadAtIsNull();
    }

    @Transactional
    @Override
    public void markAsRead(UUID id) {
        notificationRepository.markAsRead(id, LocalDateTime.now());
        // Publicar conteo actualizado por WebSocket
        publishUnreadCount();
    }

    @Transactional
    @Override
    public void markAllAsRead() {
        notificationRepository.markAllAsRead(LocalDateTime.now());
        publishUnreadCount();
    }

    // ── Helpers ───────────────────────────────────────────────────

    /** Envía el conteo de no leídas por WebSocket para actualizar la campana */
    @Override
    public void publishUnreadCount() {
        long count = countUnread();
        messagingTemplate.convertAndSend("/topic/notifications/unread-count", count);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private NotificationDto toDto(Notification n) {
        String navigateTo = null;
        if ("Employee".equals(n.getEntityType()) && n.getEntityId() != null
                && n.getType() != NotificationType.DELETED) {
            navigateTo = "/employees/" + n.getEntityId();
        }

        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .triggeredBy(n.getTriggeredBy())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .navigateTo(navigateTo)
                .build();
    }
}
