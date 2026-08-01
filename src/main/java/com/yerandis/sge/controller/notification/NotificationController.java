package com.yerandis.sge.controller.notification;

import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.dto.notification.NotificationDto;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.service.serviceInterface.notification.NotificationAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationAppService notificationAppService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getAll(
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Notificaciones obtenidas",
                        notificationAppService.getAll(page, size))
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        return ResponseEntity.ok(
                ApiResponse.success("Conteo obtenido",
                        Map.of("count", notificationAppService.countUnread()))
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationAppService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notificación marcada como leída"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationAppService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("Todas las notificaciones marcadas como leídas"));
    }
}
