package com.yerandis.sge.service.serviceImpl.admin;

import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.dto.request.admin.UserRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.admin.UserResponse;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.admin.UserMapper;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.service.serviceImpl.notification.NotificationService;
import com.yerandis.sge.service.serviceInterface.admin.UserAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserAppService {

    private static final String ENTITY_TYPE = "User";

    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;      // ← para buscar roles por ID
    private final EmployeeRepository    employeeRepository;  // ← para vincular empleado
    private final UserMapper            userMapper;
    private final PasswordEncoder       passwordEncoder;     // ← para hashear contraseña
    private final NotificationService   notificationService;

    // ── findAll ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(String search, Pageable pageable) {
        Page<User> userPage = userRepository.searchUser(search, pageable);
        Page<UserResponse> responsePage = userPage.map(userMapper::entityToResponse);
        return new PageResponse<>(responsePage);
    }

    // ── findById ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return userMapper.entityToResponse(user);
    }

    // ── create ────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        // Validación: username único
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new BusinessException(
                    "Ya existe un usuario con el nombre: " + request.getUsername()
            );
        }

        //  Validacion: email unico.
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("Ya existe un usuario con el email: " + request.getEmail()
            );
        }

        // Validación: la contraseña es obligatoria al crear
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario");
        }

        // Construir la entidad base
        User user = userMapper.requestToEntity(request);

        // Hashear la contraseña ANTES de guardar
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Buscar y asignar roles por ID
        Set<Role> roles = resolveRoles(request.getRoleIds());
        user.setRoles(roles);

        // Vincular empleado si se especificó
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(
                            "Empleado no encontrado con ID: " + request.getEmployeeId()
                    ));
            user.setEmployee(employee);
        }

        User savedUser = userRepository.save(user);

        notificationService.publishEvent(
                NotificationType.CREATED,
                "Nuevo usuario creado",
                savedUser.getUsername(),
                savedUser.getId(),
                ENTITY_TYPE
        );

        return userMapper.entityToResponse(savedUser);
    }

    // ── update ────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Verificar username único (excluyendo el usuario actual)
        String newUsername = request.getUsername().trim().toLowerCase();
        if (!user.getUsername().equals(newUsername) &&
                userRepository.existsByUsername(newUsername)) {
            throw new BusinessException(
                    "Ya existe otro usuario con el nombre: " + request.getUsername()
            );
        }

        // Actualizar campos básicos (el mapper no hashea la contraseña)
        userMapper.updateEntityFromRequest(request, user);

        // Si viene contraseña nueva, hashearla
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Actualizar roles
        user.setRoles(resolveRoles(request.getRoleIds()));

        // Actualizar empleado vinculado
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(
                            "Empleado no encontrado con ID: " + request.getEmployeeId()
                    ));
            user.setEmployee(employee);
        } else {
            user.setEmployee(null); // desvincular si se envía null
        }

        User updatedUser = userRepository.save(user);

        notificationService.publishEvent(
                NotificationType.UPDATED,
                "Usuario actualizado",
                updatedUser.getUsername(),
                updatedUser.getId(),
                ENTITY_TYPE
        );

        return userMapper.entityToResponse(updatedUser);
    }

    // ── delete ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID id) {
        // CORRECCIÓN: la condición estaba invertida
        // Antes: if (existsById) throw → lanzaba excepción si el usuario SÍ existía
        // Ahora: if (!existsById) throw → lanza excepción si NO existe
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", id);
        }

        // Cargar para obtener el nombre antes de eliminar (para la notificación)
        User toDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        String username = toDelete.getUsername();
        userRepository.deleteById(id);

        notificationService.publishEvent(
                NotificationType.DELETED,
                "Usuario eliminado",
                username,
                id,
                ENTITY_TYPE
        );
    }

    // ── toggleActive ──────────────────────────────────────────────
    // Método adicional útil para activar/desactivar sin editar todo el usuario

    @Transactional
    public UserResponse toggleActive(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        user.setActive(!user.isActive());

        User    updatedUser = userRepository.save(user);
        String  title       = updatedUser.isActive() ? "Usuario activado" : "Usuario desactivado";

        //  ->  Publicar
        notificationService.publishEvent(
                NotificationType.UPDATED,
                title,
                updatedUser.getUsername(),
                updatedUser.getId(),
                ENTITY_TYPE
        );
        return userMapper.entityToResponse(updatedUser);
    }

    // ── Helper privado: resolver roles por ID ─────────────────────

    private Set<Role> resolveRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return new HashSet<>();

        List<Role> found = roleRepository.findAllById(roleIds);

        // Verificar que todos los IDs solicitados existen
        if (found.size() != roleIds.size()) {
            throw new BusinessException("Uno o más roles especificados no existen en el sistema");
        }

        return new HashSet<>(found);
    }
}
