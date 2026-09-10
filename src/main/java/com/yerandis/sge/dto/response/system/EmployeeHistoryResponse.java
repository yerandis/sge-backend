package com.yerandis.sge.dto.response.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EmployeeHistoryResponse {
    private UUID             id;
    private String           action;
    private String           username;
    private List<Changes>    changes;
    private LocalDateTime    createdAt;
}
