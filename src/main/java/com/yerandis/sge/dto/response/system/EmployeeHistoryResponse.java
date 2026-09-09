package com.yerandis.sge.dto.response.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistoryResponse {

    UUID id;
    String action;
    String userName;
    List<Changes> changes;
    LocalDateTime createdAt;
}
