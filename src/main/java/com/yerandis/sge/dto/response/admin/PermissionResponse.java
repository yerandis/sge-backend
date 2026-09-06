package com.yerandis.sge.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private UUID   id;
    private String code;
    private String name;
    private String description;
    private String module;
}
