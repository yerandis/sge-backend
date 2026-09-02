package com.yerandis.sge.service.serviceImpl.system;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.dto.request.system.EmployeeRequest;
import com.yerandis.sge.dto.response.system.EmployeeResponse;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.entity.notification.Notification;
import com.yerandis.sge.entity.system.Department;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.system.EmployeeMapper;
import com.yerandis.sge.repository.system.DepartmentRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.service.serviceImpl.notification.NotificationService;
import com.yerandis.sge.service.serviceInterface.notification.NotificationAppService;
import com.yerandis.sge.service.serviceInterface.system.EmployeeAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Service: Spring registra esta clase como un bean de servicio.
 * @RequiredArgsConstructor (Lombok): genera constructor con todos los campos final.
 * Spring inyecta las dependencias por constructor (es la forma recomendada).
 *
 * Inyección por constructor vs @Autowired en campo:
 * - Constructor injection: las dependencias son inmutables (final), testeable,
 *   el error de dependencia circular se detecta en tiempo de inicio
 * - Field injection (@Autowired directamente): no es inmutable, más difícil de testear
 */
@Service
@RequiredArgsConstructor
public class EmployeeService implements EmployeeAppService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final NotificationService notificationService;

    /**
     * @Transactional(readOnly = true): indica que este método no modifica datos.
     * Spring optimiza la transacción: no hace flush al final, puede usar
     * conexiones de solo lectura si el driver las soporta.
     * Siempre usa readOnly = true en métodos de consulta.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findAll(
            String search,
            EmployeeStatus status,
            UUID departmentId,
            Pageable pageable) {

        Page<Employee> employeePage = employeeRepository.searchEmployees(
                search, status, departmentId, pageable
        );

//        Page<Employee> employeePage =employeeRepository.searchEmployees(pageable);

        // Convierte Page<Employee> → Page<EmployeeResponse> usando el mapper
        Page<EmployeeResponse> responsePage = employeePage
                .map(employeeMapper::toResponse);

        return new PageResponse<>(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", id));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        // Validación de negocio: el email debe ser único
        if (employeeRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("Ya existe un empleado con el email: " + request.getEmail()
            );
        }

        // Verificar que el departamento existe
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento: ", request.getDepartmentId()));

        // Convertir DTO → Entity
        Employee employee = employeeMapper.toEntity(request);
        employee.setDepartment(department);

        // Guardar en la BD
        Employee savedEmployee = employeeRepository.save(employee);

        // ← NUEVO: publicar notificación
        notificationService.publishEmployeeEvent(
                NotificationType.EMPLOYEE_CREATED,
                savedEmployee.getFirstName() + " " + savedEmployee.getLastName(),
                savedEmployee.getId()
        );

        // Convertir Entity → DTO de respuesta
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse update(UUID id, EmployeeRequest request) {
        // Verificar que el empleado existe
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado: ", id));

        // Si cambia el email, verificar que no lo usa otro empleado
        String newEmail = request.getEmail().trim().toLowerCase();
        if (!employee.getEmail().equals(newEmail) &&
                employeeRepository.existsByEmailAndIdNot(newEmail, id)) {
            throw new BusinessException(
                    "Ya existe otro empleado con el email: " + request.getEmail()
            );
        }

        // Verificar que el departamento existe
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento ", request.getDepartmentId()));

        // Actualizar los campos de la entidad existente
        employeeMapper.updateEntityFromRequest(request, employee);
        employee.setDepartment(department);

        // save() en una entidad existente (tiene ID) → hace UPDATE, no INSERT
        Employee updatedEmployee = employeeRepository.save(employee);

        notificationService.publishEmployeeEvent(
                NotificationType.EMPLOYEE_UPDATED,
                updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName(),
                updatedEmployee.getId()
        );

        return employeeMapper.toResponse(updatedEmployee);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empleado", id);
        }

        Employee toDelete = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", id));
        String fullName = toDelete.getFirstName() + " " + toDelete.getLastName();
        employeeRepository.deleteById(id);

        notificationService.publishEmployeeEvent(NotificationType.EMPLOYEE_DELETED, fullName, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("activeEmployees", employeeRepository.countByStatus(EmployeeStatus.ACTIVE));
        stats.put("inactiveEmployees", employeeRepository.countByStatus(EmployeeStatus.INACTIVE));
        stats.put("totalDepartments", departmentRepository.count());
        return stats;
    }
}
