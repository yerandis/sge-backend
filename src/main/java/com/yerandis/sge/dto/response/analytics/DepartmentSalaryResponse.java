package com.yerandis.sge.dto.response.analytics;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class DepartmentSalaryResponse {

    private final UUID id;
    private final String name;
    private final BigDecimal averageSalary;

    public DepartmentSalaryResponse(UUID id, String name, Double averageSalary) {
        this.id = id;
        this.name = name;
        this.averageSalary = averageSalary != null
                ? BigDecimal.valueOf(averageSalary).setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }
}