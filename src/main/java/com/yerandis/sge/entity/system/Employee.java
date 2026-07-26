package com.yerandis.sge.entity.system;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @UuidGenerator
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
    private String lastName;

    @Column(nullable = false, unique = true, length = 255, columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 150)
    private String position;

    /**
     * @ManyToOne: muchos empleados pertenecen a un departamento.
     * fetch = FetchType.LAZY: NO cargar el departamento automáticamente.
     * Solo cargarlo cuando realmente accedamos a employee.getDepartment().
     *
     * @JoinColumn(name = "department_id"):
     * La columna de join en la tabla employees se llama "department_id".
     * Este es el lado propietario de la relación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * BigDecimal: para valores monetarios, siempre BigDecimal en Java.
     * Igual que NUMERIC en PostgreSQL: precisión exacta, sin errores de punto flotante.
     *
     * precision = 12, scale = 2: mapea a NUMERIC(12, 2)
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    /**
     * @Enumerated(EnumType.STRING): guarda el enum como String en la BD ("ACTIVE", "INACTIVE").
     * Nunca uses EnumType.ORDINAL (guarda 0, 1, 2...).
     * Si reordenas el enum, los datos existentes se corrompen.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmployeeStatus status;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Employee() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
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
