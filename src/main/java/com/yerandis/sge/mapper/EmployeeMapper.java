package com.yerandis.sge.mapper;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.dto.request.EmployeeRequest;
import com.yerandis.sge.dto.response.DepartmentResponse;
import com.yerandis.sge.dto.response.EmployeeResponse;
import com.yerandis.sge.entity.Department;
import com.yerandis.sge.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * @Component: Spring registra esta clase como un bean y la gestiona.
 * La inyectaremos en el servicio con @Autowired o constructor injection.
 *
 * Responsabilidad única: convertir entre entidades y DTOs.
 * No tiene lógica de negocio. Solo mapea campos.
 */
@Component
public class EmployeeMapper {

    /**
     * Convierte una Entity → DTO de respuesta.
     * Se usa cuando queremos devolver datos al cliente.
     */
    public EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setPosition(employee.getPosition());
        response.setDepartment(toDepartmentResponse(employee.getDepartment()));
        response.setSalary(employee.getSalary());
        response.setStatus(employee.getStatus().name());
        response.setHireDate(employee.getHireDate());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    /**
     * Convierte un DTO de request → Entity nueva.
     * Se usa en la creación.
     * El departamento se setea en el servicio, no aquí,
     * porque necesitamos ir a la BD a buscarlo.
     */
    public Employee toEntity(EmployeeRequest request) {
        return Employee.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone())
                .position(request.getPosition().trim())
                .salary(request.getSalary())
                .status(EmployeeStatus.valueOf(request.getStatus()))
                .hireDate(request.getHireDate())
                .build();
    }

    /**
     * Actualiza una Entity existente con los datos del request.
     * Se usa en la actualización.
     * No crea una nueva Entity; modifica la que ya existe en la BD.
     */
    public void updateEntityFromRequest(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition().trim());
        employee.setSalary(request.getSalary());
        employee.setStatus(EmployeeStatus.valueOf(request.getStatus()));
        employee.setHireDate(request.getHireDate());
        // El departamento se actualiza en el servicio
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        if (department == null) return null;
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }
}
