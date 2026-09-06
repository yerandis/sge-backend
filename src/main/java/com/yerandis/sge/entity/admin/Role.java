package com.yerandis.sge.entity.admin;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad Role: colección nombrada de permisos.
 *
 * Un rol agrupa permisos bajo un nombre significativo para el negocio.
 * "Responsable RRHH" = conjunto de permisos sobre empleados y departamentos.
 *
 * La relación Role ↔ Permission es @ManyToMany:
 * - Un rol puede tener muchos permisos
 * - Un permiso puede estar en muchos roles
 *
 * FetchType.EAGER en permissions: los permisos se cargan SIEMPRE con el rol.
 * Esto es correcto aquí porque cada vez que Spring Security necesita un rol,
 * también necesita sus permisos para construir las authorities del usuario.
 * El número de permisos por rol es pequeño (< 20), así que el overhead es mínimo.
 *
 * 🔄 Si usaras LAZY: tendrías que llamar explícitamente a role.getPermissions()
 * dentro de una transacción activa, lo que complicaría el código de seguridad.
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Si true: este rol se asigna automáticamente a nuevos usuarios.
     * Solo puede haber un rol default activo al mismo tiempo.
     * Se verifica en el servicio antes de guardar.
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    /**
     * Si true: rol del sistema, no puede eliminarse.
     * El rol "Administrador" siempre es is_system = true.
     * Protege contra la eliminación accidental de roles críticos.
     */
//    @Column(name = "is_system", nullable = false)
//    private boolean isSystem = false;

    /**
     * Permisos de este rol.
     *
     * @ManyToMany: relación bidireccional a través de role_permissions.
     * FetchType.EAGER: se cargan con el rol (justificado arriba).
     * CascadeType: NO cascade aquí. Los permisos son independientes.
     * Si eliminamos un rol, NO eliminamos los permisos (son globales).
     * La tabla role_permissions sí se limpia (ON DELETE CASCADE en SQL).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
