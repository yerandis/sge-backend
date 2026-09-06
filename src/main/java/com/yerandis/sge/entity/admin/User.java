package com.yerandis.sge.entity.admin;


import com.yerandis.sge.dto.enums.UserRole;
import com.yerandis.sge.entity.system.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementa UserDetails para que Spring Security sepa cómo
 * obtener username, password y roles de nuestro usuario.
 *
 * 🔄 Comparación Java: es como implementar una interfaz que Spring conoce.
 * Nosotros definimos el contrato, Spring lo consume internamente.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Roles del usuario.
     *
     * Un usuario puede tener varios roles.
     * Un rol puede estar asignado a varios usuarios.
     *
     * FetchType.EAGER: se cargan con el usuario porque Spring Security
     * los necesita inmediatamente al autenticar.
     *
     * La tabla pivote es user_roles (a crear en el siguiente script SQL).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── UserDetails ───────────────────────────────────────────────

    /**
     * getAuthorities(): REEMPLAZA el sistema anterior de ROLE_ADMIN/ROLE_USER.
     *
     * Ahora en lugar de devolver el rol como authority,
     * devolvemos TODOS los permisos de TODOS los roles del usuario.
     *
     * Si el usuario tiene:
     *   Rol "Responsable RRHH" → permisos: EMPLOYEE_READ, EMPLOYEE_CREATE, DEPARTMENT_READ
     *   Rol "Analista" → permisos: EMPLOYEE_READ, REPORT_VIEW, REPORT_EXPORT
     *
     * getAuthorities() devuelve:
     *   [EMPLOYEE_READ, EMPLOYEE_CREATE, DEPARTMENT_READ, REPORT_VIEW, REPORT_EXPORT]
     *   (sin duplicados gracias al Set del stream)
     *
     * Spring Security verifica contra esta lista en @PreAuthorize("hasAuthority('X')")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public User() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public UserRole getRole() {
//        return role;
//    }
//
//    public void setRole(UserRole role) {
//        this.role = role;
//    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}