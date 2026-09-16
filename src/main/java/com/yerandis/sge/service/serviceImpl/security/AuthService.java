package com.yerandis.sge.service.serviceImpl.security;

import com.yerandis.sge.dto.enums.UserRole;
import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.security.AuthResponse;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.security.JwtService;
import com.yerandis.sge.service.serviceInterface.security.AuthAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * AuthService: lógica de negocio de autenticación.
 *
 * Dependencias:
 *   AuthService → UserRepository
 *   AuthService → JwtService
 *   AuthService → AuthenticationManager (de ApplicationConfig)
 *   AuthService → PasswordEncoder (de ApplicationConfig)
 *   AuthService → UserDetailsService (de ApplicationConfig)
 *
 * Ninguna de estas crea ciclos porque ApplicationConfig
 * no depende de AuthService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthAppService {

    private final UserRepository       userRepository;
    private final EmployeeRepository   employeeRepository;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder      passwordEncoder;
    private final UserDetailsService   userDetailsService;
    private final RoleRepository       roleRepository;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // 1) Delega en DaoAuthenticationProvider:
            //    carga el usuario y compara la contraseña con BCrypt.
            //    Si falla, lanza BadCredentialsException.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) userDetailsService.loadUserByUsername(request.getUsername());
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("!!!!!Login exitoso: usuario={}", user.getUsername());
            return buildAuthResponse(user);

        } catch (AuthenticationException e) {
            // Log interno con detalle, mensaje genérico al cliente
            // (nunca reveles si el usuario existe o no)
            log.warn("-----Intento de login fallido para usuario '{}': {}",
                    request.getUsername(), e.getMessage());
            throw new BusinessException("", "Usuario o contraseña incorrecta");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("", "El usuario '" + request.getUsername() + "' ya existe");
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("", "Empleado no encontrado"));
        }

        Set<Role> defaultRoles = new HashSet<>();
        roleRepository.findByIsDefaultTrue()
                .ifPresent(defaultRoles::add);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(defaultRoles)
                .employee(employee)
                .active(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new BusinessException("", "Token de refresco inválido o expirado");
        }

        String username = jwtService.extractUsername(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = (User) userDetails;

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        List<String> permissions = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .sorted()
                .toList();

        UUID employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .roles(roleNames)
                        .permissions(permissions)
                        .employeeId(employeeId)
                        .build())
                .build();
    }
}
