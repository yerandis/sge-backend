package com.yerandis.sge.mapper.admin;

import com.yerandis.sge.dto.response.admin.PermissionResponse;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import com.yerandis.sge.entity.admin.Permission;
import com.yerandis.sge.entity.admin.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role) {
        List<PermissionResponse> perms = role.getPermissions()
                .stream()
                .sorted((a, b) -> {
                    int cmp = a.getModule().compareTo(b.getModule());
                    return cmp != 0 ? cmp : a.getName().compareTo(b.getName());
                })
                .map(this::toPermissionResponse)
                .toList();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isDefault(role.isDefault())
//                .isSystem(role.isSystem())
                .permissions(perms)
                .permissionCount(perms.size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public PermissionResponse toPermissionResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .module(p.getModule())
                .build();
    }
}
