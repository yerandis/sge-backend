package com.yerandis.sge.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoleRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    private boolean isDefault = false;

    /**
     * Lista de IDs de permisos a asignar a este rol.
     * Puede ser vacía: un rol sin permisos es válido (aunque inútil).
     */
    private List<UUID> permissionIds = List.of();
}
