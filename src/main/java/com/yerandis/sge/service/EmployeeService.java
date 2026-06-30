package com.yerandis.sge.service;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.dto.request.EmployeeRequest;
import com.yerandis.sge.dto.response.EmployeeResponse;
import com.yerandis.sge.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

/**
 * Interfaz del servicio de empleados.
 *
 * ¿Por qué una interfaz? Dos razones:
 * 1. Permite tener múltiples implementaciones (ej: una real y una mock para tests)
 * 2. Desacopla el controller de la implementación concreta
 * 3. Es el contrato de lo que el servicio puede hacer
 *
 * Comparación Java: igual que definir una interface en Spring para separar
 * el contrato de la implementación.
 */
public interface EmployeeService {

    PageResponse<EmployeeResponse> findAll(
            String search,
            EmployeeStatus status,
            UUID departmentId,
            Pageable pageable
    );

    EmployeeResponse findById(UUID id);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(UUID id, EmployeeRequest request);

    void delete(UUID id);

    // Para el Dashboard
    Map<String, Long> getDashboardStats();
}
