package com.yerandis.sge.service.serviceInterface.analytics;

import com.yerandis.sge.dto.response.analytics.*;
import java.util.List;

public interface AnalyticsAppService {

    AnalyticsSummaryResponse getSummary();
    List<DepartmentHeadcountResponse> employeesByDepartment();
    List<MonthlyHireResponse> hiresByMonth(int months);
    List<DepartmentSalaryResponse> averageSalaryByDepartment();
}