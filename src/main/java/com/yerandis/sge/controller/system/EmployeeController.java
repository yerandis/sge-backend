package com.yerandis.sge.controller.system;

import com.yerandis.sge.dto.request.system.EmployeeRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.system.EmployeeResponse;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.service.serviceInterface.notification.NotificationAppService;
import com.yerandis.sge.service.serviceInterface.system.EmployeeAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * @RestController: combina @Controller + @ResponseBody.
 * Todos los métodos devuelven directamente el objeto serializado a JSON.
 *
 * @RequestMapping("/api/v1/employees"): prefijo de ruta para todos los endpoints.
 * El "/api/v1" permite versionar la API en el futuro (v2, v3...).
 *
 * @CrossOrigin: permite peticiones del frontend (puerto 5173).
 * En producción, esto se configura globalmente en CorsConfig.
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeAppService employeeAppService;


    /**
     * GET /api/v1/employees
     * GET /api/v1/employees?search=Ana&status=ACTIVE&departmentId=1&page=0&size=10&sort=lastName,asc
     *
     * @RequestParam(required = false): parámetros opcionales.
     * Si no se envían, son null y el servicio maneja ese caso.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Construir el objeto Pageable con paginación y ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Convertir el string de status a enum (si viene)
        EmployeeStatus employeeStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                employeeStatus = EmployeeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si el status no es válido, lo ignoramos y buscamos todos
            }
        }

        PageResponse<EmployeeResponse> result = employeeAppService.findAll(
                search, employeeStatus, departmentId, pageable
        );

        return ResponseEntity.ok(ApiResponse.success("Empleados obtenidos exitosamente", result));
    }

    /**
     * GET /api/v1/employees/{id}
     *
     * @PathVariable: extrae el {id} de la URL.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> findById(@PathVariable UUID id) {
        EmployeeResponse employee = employeeAppService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Empleado obtenido exitosamente ", employee));
    }

    /**
     * POST /api/v1/employees
     *
     * @RequestBody: deserializa el JSON del body a EmployeeRequest.
     * @Valid: activa las validaciones de Bean Validation en EmployeeRequest.
     * Si alguna validación falla, Spring lanza MethodArgumentNotValidException
     * que el GlobalExceptionHandler captura.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse employee = employeeAppService.create(request);
        // 201 Created: el recurso fue creado exitosamente
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Empleado creado exitosamente", employee));
    }

    /**
     * PUT /api/v1/employees/{id}
     * Actualización completa del recurso.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse employee = employeeAppService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Empleado actualizado exitosamente", employee));
    }

    /**
     * DELETE /api/v1/employees/{id}
     * 204 No Content: operación exitosa, sin body en la respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeAppService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * GET /api/v1/employees/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboardStats() {
        Map<String, Long> stats = employeeAppService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", stats));
    }
}
