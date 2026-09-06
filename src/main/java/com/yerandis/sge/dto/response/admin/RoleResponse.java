package com.yerandis.sge.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private UUID                    id;
    private String                  name;
    private String                  description;
    private boolean                 isDefault;
//    private boolean                 isSystem;
    private List<PermissionResponse> permissions;
    private int                     permissionCount;
    private LocalDateTime           createdAt;
    private LocalDateTime           updatedAt;
}
