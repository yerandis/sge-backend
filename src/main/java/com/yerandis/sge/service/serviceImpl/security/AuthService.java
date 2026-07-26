package com.yerandis.sge.service.serviceImpl.security;

import com.yerandis.sge.dto.enums.UserRole;
import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.security.AuthResponse;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.security.JwtService;
import com.yerandis.sge.service.serviceInterface.security.AuthServiceApp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class AuthService implements AuthServiceApp {

    private final UserRepository       userRepository;
    private final EmployeeRepository   employeeRepository;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder      passwordEncoder;
    private final UserDetailsService   userDetailsService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            /**
             * authenticate() delega en DaoAuthenticationProvider (ApplicationConfig):
             * 1. Llama userDetailsService.loadUserByUsername(username)
             * 2. Compara la contraseña con BCrypt
             * 3. Si falla → BadCredentialsException
             */
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            // Mensaje genérico: no revelar si el usuario existe o no
            throw new BusinessException("Credenciales inválidas");
        }

        // Cargar el usuario para generar el token con sus datos completos
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = (User) userDetails;

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El usuario '" + request.getUsername() + "' ya existe");
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("Empleado no encontrado"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .employee(employee)
                .active(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new BusinessException("Token de refresco inválido o expirado");
        }

        String username = jwtService.extractUsername(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = (User) userDetails;

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UUID employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .employeeId(employeeId)
                        .build())
                .build();
    }
}
