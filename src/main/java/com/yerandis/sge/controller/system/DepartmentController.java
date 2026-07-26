package com.yerandis.sge.controller.system;


import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import com.yerandis.sge.repository.system.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> findAll() {
        List<DepartmentResponse> departments = departmentRepository.findAll()
                .stream()
                .map(d -> new DepartmentResponse(d.getId(), d.getName(), d.getDescription()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Departamentos obtenidos", departments));
    }
}
