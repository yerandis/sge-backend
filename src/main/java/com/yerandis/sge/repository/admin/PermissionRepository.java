package com.yerandis.sge.repository.admin;

import com.yerandis.sge.entity.admin.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Todos los permisos ordenados por módulo y luego por nombre.
     * Útil para la UI de asignación de permisos a roles.
     */
    List<Permission> findAllByOrderByModuleAscNameAsc();

    /**
     * Permisos de un módulo específico.
     */
    List<Permission> findByModuleOrderByNameAsc(String module);

    /**
     * Lista de módulos únicos (para la UI de agrupación).
     */
    @Query("SELECT DISTINCT p.module FROM Permission p ORDER BY p.module ASC")
    List<String> findDistinctModules();

    /**
     * Buscar permisos por IDs (para asignar a un rol desde la UI).
     */
    List<Permission> findByIdIn(List<UUID> ids);
}
