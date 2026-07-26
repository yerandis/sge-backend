package com.yerandis.sge.service.serviceInterface.security;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.security.AuthResponse;
import jakarta.validation.Valid;

public interface AuthServiceApp {
    AuthResponse login(@Valid LoginRequest request);

    AuthResponse register(@Valid RegisterRequest request);

    AuthResponse refreshToken(String refreshToken);
}
