package com.yerandis.sge.mapper.system;

import com.yerandis.sge.dto.request.system.DepartmentRequest;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import com.yerandis.sge.entity.system.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    /** Entity -> dto de respuesta al cliente **/

    public DepartmentResponse toResponse(Department department){

        DepartmentResponse response = new DepartmentResponse();

        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());

        return response;
    }

    /**  Request -> Entity  (para la creacion) **/

    public Department toEntity(DepartmentRequest request){

        return Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    /** actualizar Entity con datos del request  **/
    public void updateEntityFromRequest(DepartmentRequest request, Department department){
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription().trim());
    }
}
