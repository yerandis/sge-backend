package com.yerandis.sge.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de entrada para crear y actualizar empleados.
 * Las anotaciones de validación de Bean Validation aseguran que los datos
 * lleguen correctos antes de entrar al servicio.
 *
 * ¿Por qué un solo Request para crear Y actualizar?
 * Porque los campos requeridos son los mismos. Si en el futuro difieren
 * significativamente, crearemos CreateEmployeeRequest y UpdateEmployeeRequest.
 */
@Getter
@Setter
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String phone;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 150, message = "El cargo no puede superar los 150 caracteres")
    private String position;

    @NotNull(message = "El departamento es obligatorio")
    private UUID departmentId;

    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2,
            message = "El salario debe tener como máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal salary;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVE|INACTIVE",
            message = "El estado debe ser ACTIVE o INACTIVE")
    private String status;

    @NotNull(message = "La fecha de contratación es obligatoria")
    @PastOrPresent(message = "La fecha de contratación no puede ser futura")
    private LocalDate hireDate;
}
