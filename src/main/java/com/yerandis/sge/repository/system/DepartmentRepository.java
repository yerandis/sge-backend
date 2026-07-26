package com.yerandis.sge.repository.system;


import com.yerandis.sge.entity.system.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * JpaRepository<Department, UUID>:
 * - Department: el tipo de la entidad
 * - UUID: el tipo del ID
 *
 * Spring Data genera automáticamente la implementación en tiempo de ejecución.
 * Ya tienes disponibles: findAll(), findById(), save(), deleteById(), count()...
 * Sin escribir ni una línea de SQL.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
