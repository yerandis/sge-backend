package com.yerandis.sge.controller.system;


import com.yerandis.sge.dto.request.system.DepartmentRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import com.yerandis.sge.service.serviceInterface.system.DepartmentAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentAppService departmentAppService;

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> findAll() {
//        List<DepartmentResponse> departments = departmentRepository.findAll()
//                .stream()
//                .map(d -> new DepartmentResponse(d.getId(), d.getName(), d.getDescription()))
//                .toList();
//        return ResponseEntity.ok(ApiResponse.success("Departamentos obtenidos", departments));
//    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size,
            @RequestParam(defaultValue = "name")String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        // Se construye el objrto pageable con paginacion y ordenamineto
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DepartmentResponse> result = departmentAppService.findAll(search, pageable);

        return ResponseEntity.ok(ApiResponse.success("Departamentos obtenidos exitosamente: ", result));
    }

    /**
     * GET /api/v1/department/{id}
     * @PathVariable: id
     * extrae el id de la URL
     */

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> findById(@PathVariable("id")UUID id){

        DepartmentResponse response = departmentAppService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Departamento obtenido exitosamente: ", response));
    }

    /**
     * POST /api/v1/department
     * @RequestBody: deserializa el JSON del body a DepartmentRequest.
     * @Valid: activa las validaciones de Bean Validation en DepartmentRequest.
     * Si alguna validación falla, Spring lanza MethodArgumentNotValidException
     * que el GlobalExceptionHandler captura.
     *
     * */
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentRequest request){

        DepartmentResponse response = departmentAppService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Departamento creado exitosamente: ", response));
    }

    /**
     *
     * PUT /api/v1/department/{id}
     * Actualizacion
     *
     * */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody DepartmentRequest request
    ){
        DepartmentResponse response = departmentAppService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Departamento actualizado exitosamente: ", response));
    }

    /**
     *
     * DELETE /api/v1/department/{id}
     *
     * */
    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable ("id") UUID id){

        departmentAppService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *
     * GET /api/v1/department/dashboard/stats
     * */
    @GetMapping("dashboard/stats")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboardStats(){
        Map<String, Long> stats = departmentAppService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Estadisticas obtenidas: ", stats));
    }
}
