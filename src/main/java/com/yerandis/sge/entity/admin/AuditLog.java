package com.yerandis.sge.entity.admin;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad AuditLog: registra cada acción relevante del sistema.
 *
 * Campos old_value y new_value: JSON serializado como String.
 * Se usan JSONB en PostgreSQL para poder hacer búsquedas sobre ellos,
 * pero en Java los tratamos como String para simplicidad.
 *
 * No tiene updated_at porque los logs de auditoría son inmutables:
 * una vez creados, nunca se modifican.
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    /**
     * Tipo de entidad sobre la que se realizó la acción.
     * Convención: nombre en mayúsculas del recurso: "EMPLOYEE", "USER", "DEPARTMENT"
     */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /**
     * UUID de la entidad afectada.
     * Ejemplo: el UUID del empleado que fue editado.
     */
    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    /**
     * Acción realizada: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Username del usuario que realizó la acción.
     * String en lugar de FK a User para preservar el historial
     * incluso si el usuario es eliminado después.
     */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /**
     * Estado del objeto ANTES del cambio (JSON serializado).
     * null en operaciones CREATE (no había estado previo).
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * Estado del objeto DESPUÉS del cambio (JSON serializado).
     * null en operaciones DELETE (no hay estado posterior).
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * IP del cliente que realizó la acción.
     * Útil para auditorías de seguridad.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Módulo del sistema: "EMPLOYEES", "USERS", "DEPARTMENTS", etc.
     */
    @Column(name = "module", length = 50)
    private String module;

    /**
     * Resultado de la acción: SUCCESS, FAILED, UNAUTHORIZED.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "SUCCESS";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
