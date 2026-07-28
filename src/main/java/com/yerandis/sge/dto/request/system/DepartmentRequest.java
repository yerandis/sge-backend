package com.yerandis.sge.dto.request.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** dto de entrada para crear y actualizar Department **/

@Getter
@Setter
@NoArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "El nombre del departamento es obligtorio")
    private String name;

    @Size(max = 500)
    private String description;
}
