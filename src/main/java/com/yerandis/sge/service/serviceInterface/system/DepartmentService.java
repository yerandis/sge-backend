package com.yerandis.sge.service.serviceInterface.system;

import com.yerandis.sge.dto.request.system.DepartmentRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface DepartmentService {

    PageResponse<DepartmentResponse> findAll(String search, Pageable pageable);

    DepartmentResponse findById(UUID id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(UUID id, DepartmentRequest request);

    void delete(UUID id);

    Map<String, Long> getDashboardStats();
}
