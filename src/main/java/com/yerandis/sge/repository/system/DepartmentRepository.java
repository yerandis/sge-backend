package com.yerandis.sge.repository.system;


import com.yerandis.sge.dto.response.analytics.DepartmentHeadcountResponse;
import com.yerandis.sge.dto.response.analytics.DepartmentSalaryResponse;
import com.yerandis.sge.entity.system.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(
            value = """
SELECT e
FROM Department e
WHERE (
    :search IS NULL
    OR LOWER(CAST(e.name AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
)
""",
            countQuery = """
                    SELECT COUNT(e)
                    FROM Department e
                    WHERE (
                                :search IS NULL
                                OR LOWER(CAST(e.name AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                            )
                    """
    )
    Page<Department>searchDepartment(@Param("search") String search, Pageable pageable);

    @Query("""
    SELECT new com.yerandis.sge.dto.response.analytics.DepartmentHeadcountResponse(
        d.id, d.name, COUNT(e.id))
    FROM Department d
    LEFT JOIN d.employees e
    GROUP BY d.id, d.name
    ORDER BY COUNT(e.id) DESC
    """)
    List<DepartmentHeadcountResponse> findEmployeeCountByDepartment();

    @Query("""
    SELECT new com.yerandis.sge.dto.response.analytics.DepartmentSalaryResponse(
        d.id, d.name, AVG(e.salary))
    FROM Department d
    JOIN d.employees e
    WHERE e.status = com.yerandis.sge.dto.enums.EmployeeStatus.ACTIVE
    GROUP BY d.id, d.name
    ORDER BY AVG(e.salary) DESC
    """)
    List<DepartmentSalaryResponse> findAverageSalaryByDepartment();
}
