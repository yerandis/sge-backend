package com.yerandis.sge.controller.analytics;

import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.analytics.*;
import com.yerandis.sge.service.serviceInterface.analytics.AnalyticsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsAppService analyticsAppService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success("Resumen obtenido", analyticsAppService.getSummary()));
    }

    @GetMapping("/employees/by-department")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<List<DepartmentHeadcountResponse>>> employeesByDepartment() {
        return ResponseEntity.ok(ApiResponse.success("Datos obtenidos", analyticsAppService.employeesByDepartment()));
    }

    @GetMapping("/employees/hires-by-month")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<List<MonthlyHireResponse>>> hiresByMonth(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(ApiResponse.success("Datos obtenidos", analyticsAppService.hiresByMonth(months)));
    }

    @GetMapping("/departments/average-salary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<List<DepartmentSalaryResponse>>> averageSalaryByDepartment() {
        return ResponseEntity.ok(ApiResponse.success("Datos obtenidos", analyticsAppService.averageSalaryByDepartment()));
    }
}