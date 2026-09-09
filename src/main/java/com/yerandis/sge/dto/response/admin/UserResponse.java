package com.yerandis.sge.dto.response.admin;

import com.yerandis.sge.dto.response.system.EmployeeResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID                 id;
    private String               username;
    private String               email;
    private String               firstName;
    private String               lastName;
    private String               avatarUrl;
    private boolean              active;
    private List<RoleResponse>   roles;
    private EmployeeResponse     employee;   // null si no está vinculado a un empleado
    private LocalDateTime        lastLogin;
    private LocalDateTime        createdAt;
    private LocalDateTime        updatedAt;
}
