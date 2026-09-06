package com.yerandis.sge.service.serviceImpl.admin;

import com.yerandis.sge.dto.request.admin.RoleRequest;
import com.yerandis.sge.dto.response.admin.PermissionResponse;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import com.yerandis.sge.entity.admin.Permission;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.repository.admin.PermissionRepository;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.service.serviceInterface.admin.RoleAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService implements RoleAppService {

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;

    // ── Roles ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public List<RoleResponse> findAllRoles() {
        return roleRepository.findAllWithPermissions()
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public RoleResponse findRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));
        return toRoleResponse(role);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public RoleResponse createRole(RoleRequest request) {
        // Verificar nombre único
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException("Ya existe un rol con el nombre: " + request.getName());
        }

        // Si este rol se marca como default, quitar el default del anterior
        if (request.isDefault()) {
            roleRepository.findByIsDefaultTrue()
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        roleRepository.save(existing);
                    });
        }

        // Cargar los permisos seleccionados
        List<Permission> permissions = permissionRepository.findByIdIn(request.getPermissionIds());

        Role role = Role.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isDefault(request.isDefault())
//                .isSystem(false)  // Solo el sistema puede crear roles de sistema
                .permissions(new HashSet<>(permissions))
                .build();

        return toRoleResponse(roleRepository.save(role));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public RoleResponse updateRole(UUID id, RoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));

        // Verificar nombre único (excluyendo el rol actual)
        if (roleRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException("Ya existe otro rol con el nombre: " + request.getName());
        }

        // Gestionar el flag default
        if (request.isDefault() && !role.isDefault()) {
            roleRepository.findByIsDefaultTrue()
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        roleRepository.save(existing);
                    });
        }

        // Cargar y actualizar permisos
        List<Permission> permissions = permissionRepository.findByIdIn(request.getPermissionIds());

        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());
        role.setDefault(request.isDefault());
        role.setPermissions(new HashSet<>(permissions));

        return toRoleResponse(roleRepository.save(role));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public void deleteRole(UUID id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));

        // Los roles del sistema no pueden eliminarse
//        if (role.isSystem()) {
//            throw new BusinessException(
//                    "El rol '" + role.getName() + "' es un rol del sistema y no puede eliminarse."
//            );
//        }

        roleRepository.deleteById(id);
    }

    // ── Permisos ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public List<PermissionResponse> findAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscNameAsc()
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    // ── Mappers privados ──────────────────────────────────────────

    private RoleResponse toRoleResponse(Role role) {
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

    private PermissionResponse toPermissionResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .module(p.getModule())
                .build();
    }
}
