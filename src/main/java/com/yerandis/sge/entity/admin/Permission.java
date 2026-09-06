package com.yerandis.sge.entity.admin;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Permission: representa un permiso granular del sistema.
 *
 * Los permisos son GLOBALES: no pertenecen a ninguna empresa específica.
 * Los define el desarrollador. El admin los asigna a roles.
 *
 * El campo 'code' es lo que se usa en @PreAuthorize:
 * @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
 *
 * 🔄 Comparación con Java EE:
 * Equivale a una entrada en un archivo de políticas de seguridad,
 * pero gestionada en base de datos en lugar de en archivos de configuración.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    /**
     * Código técnico único: EMPLOYEE_READ, DEPARTMENT_DELETE, etc.
     * Convención: RECURSO_ACCION en mayúsculas con guión bajo.
     * Este código es el que verifica Spring Security en cada petición.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Nombre legible para mostrar en la UI de administración.
     * "Ver empleados", "Crear departamentos", etc.
     */
    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Módulo al que pertenece: EMPLOYEES, DEPARTMENTS, USERS, ROLES, REPORTS, SYSTEM.
     * Se usa en la UI para agrupar los permisos por módulo.
     */
    @Column(nullable = false, length = 50)
    private String module;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}