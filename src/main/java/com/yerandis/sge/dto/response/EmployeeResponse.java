package com.yerandis.sge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida: lo que el cliente (React) recibe cuando pide un empleado.
 *
 * Observa que incluye el departamento completo (DepartmentResponse),
 * no solo el department_id. El frontend necesita mostrar el nombre del
 * departamento, no solo su ID.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private DepartmentResponse department;
    private BigDecimal salary;
    private String status;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
