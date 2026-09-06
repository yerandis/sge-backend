package com.yerandis.sge.service.serviceInterface.admin;

import com.yerandis.sge.dto.request.admin.RoleRequest;
import com.yerandis.sge.dto.response.admin.PermissionResponse;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface RoleAppService {
    
    List<RoleResponse> findAllRoles();

    RoleResponse findRoleById(UUID id);

    RoleResponse createRole(@Valid RoleRequest request);

    RoleResponse updateRole(UUID id, @Valid RoleRequest request);

    void deleteRole(UUID id);

    List<PermissionResponse> findAllPermissions();
}
