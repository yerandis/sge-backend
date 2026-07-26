package com.yerandis.sge.entity.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un departamento de la empresa.
 *
 * @Table(name = "departments"): mapea esta clase a la tabla "departments" en PostgreSQL.
 * Si no especificas @Table, Hibernate usa el nombre de la clase en minúsculas.
 * Siempre especifícalo explícitamente para evitar sorpresas.
 *
 * @EntityListeners(AuditingEntityListener.class): activa el sistema de auditoría
 * de Spring Data JPA. Necesario para @CreatedDate y @LastModifiedDate.
 */
@Entity
@Table(name = "departments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
public class Department {

    @Id
    @UuidGenerator
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * @OneToMany: un departamento tiene muchos empleados.
     * mappedBy = "department": el lado propietario de la relación es Employee.department.
     * El campo en Employee que define la relación se llama "department".
     *
     * fetch = FetchType.LAZY: los empleados NO se cargan automáticamente
     * cuando cargas un departamento. Se cargan solo cuando accedes a esta lista.
     * SIEMPRE usa LAZY en colecciones. EAGER en colecciones es un problema de rendimiento.
     *
     * cascade = CascadeType.ALL: si eliminas un departamento, se eliminan sus empleados.
     * CUIDADO: en el SGE no queremos esto. Lo configuraremos correctamente más adelante.
     * Por ahora lo dejamos sin cascade.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Department() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
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