package com.yerandis.sge.dto.request.admin;

import com.yerandis.sge.entity.admin.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos"
    )
    private String username;

    /**
     * Contraseña en texto plano. Llegará aquí sin hashear.
     * El servicio la hashea con BCrypt antes de guardarla.
     * En actualizaciones: si viene null o vacío, NO se cambia la contraseña.
     */
    private String password;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email incorrecto")
    private String email;

    private String               firstName;
    private String               lastName;
    private String               avatarUrl = "";
    private boolean active = true;

    // -- IDs de roles a asignar. Puede ser lista vacía.
    private List<UUID> roleIds = List.of();

    /**
     * ID del empleado a vincular (opcional).
     * Un usuario puede existir sin estar vinculado a un empleado.
     */
    private UUID employeeId;
}
