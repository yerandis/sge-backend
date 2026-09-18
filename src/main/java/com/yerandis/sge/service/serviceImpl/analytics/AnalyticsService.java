package com.yerandis.sge.service.serviceImpl.analytics;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.dto.response.analytics.*;
import com.yerandis.sge.repository.system.DepartmentRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.service.serviceInterface.analytics.AnalyticsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService implements AnalyticsAppService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {
        return AnalyticsSummaryResponse.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByStatus(EmployeeStatus.ACTIVE))
                .inactiveEmployees(employeeRepository.countByStatus(EmployeeStatus.INACTIVE))
                .totalDepartments(departmentRepository.count())
                .hiresLast30Days(employeeRepository.countByHireDateAfter(LocalDate.now().minusDays(30)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentHeadcountResponse> employeesByDepartment() {
        return departmentRepository.findEmployeeCountByDepartment();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyHireResponse> hiresByMonth(int months) {
        LocalDate from = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        List<LocalDate> hireDates = employeeRepository.findHireDatesFrom(from);

        // Inicializa todos los meses del rango en 0 para no dejar huecos en el gráfico
        Map<YearMonth, Long> counts = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(from);
        YearMonth end = YearMonth.now();
        while (!cursor.isAfter(end)) {
            counts.put(cursor, 0L);
            cursor = cursor.plusMonths(1);
        }

        for (LocalDate date : hireDates) {
            YearMonth ym = YearMonth.from(date);
            counts.merge(ym, 1L, Long::sum);
        }

        List<MonthlyHireResponse> result = new ArrayList<>();
        counts.forEach((ym, count) ->
                result.add(new MonthlyHireResponse(ym.format(MONTH_FORMAT), count)));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentSalaryResponse> averageSalaryByDepartment() {
        return departmentRepository.findAverageSalaryByDepartment();
    }
}