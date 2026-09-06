package com.yerandis.sge.service.serviceImpl.admin;

import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.dto.request.admin.RoleRequest;
import com.yerandis.sge.dto.response.admin.PermissionResponse;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import com.yerandis.sge.entity.admin.Permission;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.admin.RoleMapper;
import com.yerandis.sge.repository.admin.PermissionRepository;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.service.serviceImpl.notification.NotificationService;
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

    private static final String ENTITY_TYPE = "Rol";

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;
    private final NotificationService  notificationService;
    private final RoleMapper           roleMapper;

    // ── Roles ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
//    @PreAuthorize("hasAuthority('ROLE_READ')")
    public List<RoleResponse> findAllRoles() {
        return roleRepository.findAllWithPermissions()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
//    @PreAuthorize("hasAuthority('ROLE_READ')")
    public RoleResponse findRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));
        return roleMapper.toRoleResponse(role);
    }

    @Transactional
//    @PreAuthorize("hasAuthority('ROLE_CREATE')")
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

        Role savedRole = roleRepository.save(role);

        // ← NUEVO: publicar notificación
        notificationService.publishEvent(
                NotificationType.CREATED,
                "Nuevo rol registrado",
                savedRole.getName(),
                savedRole.getId(), ENTITY_TYPE
        );

        return roleMapper.toRoleResponse(savedRole);
    }

    @Transactional
//    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
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

        Role updatedRole = roleRepository.save(role);

        notificationService.publishEvent(
                NotificationType.UPDATED,
                "Rol actualizado",
                updatedRole.getName(),
                updatedRole.getId(), ENTITY_TYPE
        );

        return roleMapper.toRoleResponse(updatedRole);
    }

    @Transactional
//    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public void deleteRole(UUID id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));

        // Los roles del sistema no pueden eliminarse
//        if (role.isSystem()) {
//            throw new BusinessException(
//                    "El rol '" + role.getName() + "' es un rol del sistema y no puede eliminarse."
//            );
//        }
        String roleName = role.getName();
        roleRepository.deleteById(id);
        notificationService.publishEvent(
                NotificationType.DELETED,
                "Rol eliminado",
                roleName, id, ENTITY_TYPE);
    }

    // ── Permisos ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
//    @PreAuthorize("hasAuthority('ROLE_READ')")
    public List<PermissionResponse> findAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscNameAsc()
                .stream()
                .map(roleMapper::toPermissionResponse)
                .toList();
    }
}
