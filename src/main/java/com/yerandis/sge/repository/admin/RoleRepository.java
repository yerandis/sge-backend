package com.yerandis.sge.repository.admin;

import com.yerandis.sge.entity.admin.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    List<Role> findAllByOrderByNameAsc();

    /**
     * El rol marcado como default (para asignar automáticamente a nuevos usuarios).
     */
    Optional<Role> findByIsDefaultTrue();

    /**
     * Roles con conteo de permisos y usuarios (para la tabla de administración).
     *
     * Usamos una query JPQL con subconsultas porque necesitamos
     * información de tablas relacionadas de forma eficiente.
     */
    @Query("""
        SELECT r FROM Role r
        LEFT JOIN FETCH r.permissions
        ORDER BY r.name ASC
        """)
    List<Role> findAllWithPermissions();
}
