package com.yerandis.sge.controller.security;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.security.AuthResponse;
import com.yerandis.sge.service.serviceInterface.security.AuthAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;

    /**
     * POST /api/v1/auth/login
     * Ruta pública (SecurityConfig.PUBLIC_ROUTES)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authAppService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    /**
     * POST /api/v1/auth/register
     * En producción: proteger con hasRole('ADMIN')
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authAppService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente: ", response));
    }

    /**
     * POST /api/v1/auth/refresh
     * Body: { "refreshToken": "eyJ..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El refresh token es obligatorio"));
        }
        AuthResponse response = authAppService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token renovado", response));
    }

    /**
     * POST /api/v1/auth/logout
     * Con JWT: el logout es responsabilidad del cliente (borrar el token).
     * Este endpoint confirma el logout y puede usarse para registrar el evento.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Sesión cerrada exitosamente"));
    }
}