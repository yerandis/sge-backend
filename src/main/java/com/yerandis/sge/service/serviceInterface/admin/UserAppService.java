package com.yerandis.sge.service.serviceInterface.admin;

import com.yerandis.sge.dto.request.admin.UserRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.admin.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserAppService {

    PageResponse<UserResponse> findAll(String search, Pageable pageable);
    UserResponse findById(UUID id);
    UserResponse create(UserRequest request);
    UserResponse update(UUID id, UserRequest request);
    void delete(UUID id);
    UserResponse toggleActive(UUID id);
}
