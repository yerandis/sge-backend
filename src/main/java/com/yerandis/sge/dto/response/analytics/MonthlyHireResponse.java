package com.yerandis.sge.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyHireResponse {

    private String month;   // formato "YYYY-MM"
    private long hires;
}