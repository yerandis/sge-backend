package com.yerandis.sge.dto.response.analytics;

import lombok.Getter;
import java.util.UUID;

@Getter
public class DepartmentHeadcountResponse {
    private final UUID id;
    private final String name;
    private final long employeeCount;

    public DepartmentHeadcountResponse(UUID id, String name, Long employeeCount) {
        this.id = id;
        this.name = name;
        this.employeeCount = employeeCount != null ? employeeCount : 0L;
    }
}