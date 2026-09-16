package com.yerandis.sge.controller.admin;

import com.yerandis.sge.dto.request.admin.UserRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.admin.UserResponse;
import com.yerandis.sge.service.serviceInterface.admin.UserAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

        private final UserAppService userAppService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAll(
            @RequestParam (required = false)            String  search,
            @RequestParam (defaultValue = "0")          int     page,
            @RequestParam (defaultValue = "10")         int     size,
            @RequestParam (defaultValue = "username")   String  sortBy,
            @RequestParam (defaultValue = "asc")        String  sortDir
    ){

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<UserResponse> result = userAppService.findAll(
                search, pageable);

        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos exitosamente", result));
    }

    /**
     * Get api/v1/users/{id}
     * @PathVariable: extrae el id de la URL
     * */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> findById(
            @PathVariable ("id")UUID id
            ){
        UserResponse userResponse = userAppService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario encontrado satisfactoriamente", userResponse));
    }

    /**
     * POST /api/v1/users
     * @RequestBody
     *
     * */
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserRequest request
    ){
        UserResponse response = userAppService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario creado exitosamente", response));
    }

    /**
     * PUT api/v1/user/{id}
     * @PathVariable
     * @Body
     * */
    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable("id")UUID id,
            @Valid @RequestBody UserRequest request
    ){
        UserResponse response = userAppService.update(id, request);

        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado exitosamente", response));
    }

    /**
     *
     * DELETE /api/v1/users/{id}
     * */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID id){
        userAppService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Estado actualizado", userAppService.toggleActive(id))
        );
    }
}
