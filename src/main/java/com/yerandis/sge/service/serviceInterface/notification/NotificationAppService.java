package com.yerandis.sge.service.serviceInterface.notification;

import com.yerandis.sge.dto.dto.notification.NotificationDto;
import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.dto.response.admin.PageResponse;

import java.util.UUID;

public interface NotificationAppService {

    void publishEmployeeEvent(NotificationType type, String employeeName, UUID employeeId);
    void publish(NotificationType type, String title, String message, String entityType, UUID entityId, String triggeredBy);
    PageResponse<NotificationDto> getAll(int page, int size);
    long countUnread();
    void markAsRead(UUID id);
    void markAllAsRead();
    void publishUnreadCount();
}
