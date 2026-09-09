package com.yerandis.sge.mapper.admin;

import com.yerandis.sge.dto.request.admin.UserRequest;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import com.yerandis.sge.dto.response.admin.UserResponse;
import com.yerandis.sge.dto.response.system.EmployeeResponse;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.mapper.system.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleMapper      roleMapper;
    private final EmployeeMapper  employeeMapper;

    /**
     * Entity → DTO de respuesta.
     * No incluye password.
     */
    public UserResponse entityToResponse(User user) {
        // Convertir Set<Role> → List<RoleResponse>
        List<RoleResponse> roleResponses = user.getRoles()
                .stream()
                .sorted(Comparator.comparing(Role::getName))  // orden alfabético
                .map(roleMapper::toRoleResponse)
                .toList();

        // Convertir Employee → EmployeeResponse (puede ser null)
        EmployeeResponse employeeResponse = null;
        if (user.getEmployee() != null) {   // ← verificar null antes de mapear
            employeeResponse = employeeMapper.toResponse(user.getEmployee());
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                // password NO se incluye
                .active(user.isActive())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleResponses)
                .employee(employeeResponse)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Request → Entity nueva (para crear).
     * La contraseña se pasa sin hashear — el servicio la hashea.
     * Los roles NO se asignan aquí — el servicio los busca por ID.
     */
    public User requestToEntity(UserRequest request) {
        return User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .password(request.getPassword())    // el servicio la hashea antes de guardar
                .active(request.isActive())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .avatarUrl(request.getAvatarUrl())
                // roles y employee los setea el servicio
                .build();
    }

    /**
     * Actualiza una Entity existente con los datos del request.
     * Solo actualiza los campos que no son null/vacíos.
     * Los roles y employee los actualiza el servicio.
     */
    public void updateEntityFromRequest(UserRequest request, User user) {
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setActive(request.isActive());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAvatarUrl(request.getAvatarUrl());
        // La contraseña solo se actualiza si viene en el request
        // Si es null o vacía, se mantiene la contraseña actual
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword()); // el servicio la hashea
        }
        // roles y employee los actualiza el servicio
    }
}
