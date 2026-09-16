package com.yerandis.sge.service.serviceImpl.system;

import com.yerandis.sge.dto.enums.NotificationType;
import com.yerandis.sge.dto.request.system.DepartmentRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import com.yerandis.sge.entity.system.Department;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.system.DepartmentMapper;
import com.yerandis.sge.repository.system.DepartmentRepository;
import com.yerandis.sge.service.serviceImpl.notification.NotificationService;
import com.yerandis.sge.service.serviceInterface.system.DepartmentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService implements DepartmentAppService {

    private static final String ENTITY_TYPE = "Department";

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper     departmentMapper;
    private final NotificationService  notificationService;

    //  Transactional(readOnly = true) -> este metodo no modifica datos
    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> findAll(String search, Pageable pageable){

        Page<Department> departmentPage = departmentRepository.searchDepartment(search, pageable);
        Page<DepartmentResponse> responsePage = departmentPage.map(departmentMapper::toResponse);

        return new PageResponse<>(responsePage);
    }

    @Override
    @Transactional
    public DepartmentResponse findById(UUID id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: ",id));

        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request){

        // validando el nombre, debe ser unico
        if (departmentRepository.existsByName(request.getName().trim().toLowerCase())){
            throw new BusinessException("", "Ya existe un departamento con el nombre " + request.getName());
        }

        Department department = departmentMapper.toEntity(request);
        Department savedDepartment = departmentRepository.save(department);

        // ← NUEVO: publicar notificación
        notificationService.publishEvent(
                NotificationType.CREATED,
                "Nuevo departamento registrado",
                savedDepartment.getName(),
                savedDepartment.getId(), ENTITY_TYPE
        );

        return departmentMapper.toResponse(savedDepartment);

//        return departmentMapper.toResponse(departmentRepository.save(departmentMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request){

        //  verificar que el departmen exist
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento " + request.getName()+  " no encontrado"));

        //  actualizar los campos de la entidad existentes
        departmentMapper.updateEntityFromRequest(request, department);
        Department updateDepartment = departmentRepository.save(department);

        notificationService.publishEvent(
                NotificationType.UPDATED,
                "Departamento actualizado",
                updateDepartment.getName(),
                updateDepartment.getId(), ENTITY_TYPE
        );

        return departmentMapper.toResponse(updateDepartment);
//        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Departamento: ", id);
        }

        Department toDelete = departmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));
        String departmentName = toDelete.getName();
        departmentRepository.deleteById(id);

        notificationService.publishEvent(
                NotificationType.DELETED,
                "Departamento eliminado",
                departmentName, id, ENTITY_TYPE);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalDepartments", departmentRepository.count());
        return stats;
    }
}
