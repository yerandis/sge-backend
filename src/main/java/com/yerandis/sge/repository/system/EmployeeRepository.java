package com.yerandis.sge.repository.system;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.entity.system.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    /**
     * Spring Data interpreta el nombre del método y genera el SQL automáticamente.
     * findBy → SELECT ... FROM employees WHERE
     * StatusAndDepartmentId → status = ? AND department_id = ?
     * Pageable → agrega LIMIT/OFFSET y ORDER BY
     *
     * Hibernate genera:
     * SELECT * FROM employees
     * WHERE status = ? AND department_id = ?
     * ORDER BY last_name ASC
     * LIMIT 10 OFFSET 0
     */
    Page<Employee> findByStatusAndDepartmentId(
            EmployeeStatus status,
            UUID departmentId,
            Pageable pageable
    );

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    Page<Employee> findByDepartmentId(UUID departmentId, Pageable pageable);

    /**
     * @Query con JPQL: cuando el nombre del método no es suficiente para
     * expresar la consulta, usamos JPQL (Java Persistence Query Language).
     *
     * JPQL trabaja con nombres de entidades y campos de Java, NO con
     * nombres de tablas y columnas de SQL.
     *
     * LOWER() e ILIKE: búsqueda case-insensitive.
     *
     * El JOIN FETCH department: carga el departamento en la misma query
     * para evitar el problema N+1 (ver explicación abajo).
     */
    @Query(
            value = """
    SELECT e
    FROM Employee e
    JOIN FETCH e.department d
    WHERE (
        :search IS NULL
        OR LOWER(CAST(e.firstName AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
        OR LOWER(CAST(e.lastName AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
        OR LOWER(CAST(e.email AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
    )
    AND (:status IS NULL OR e.status = :status)
    AND (:departmentId IS NULL OR d.id = :departmentId)
    """,
            countQuery = """
    SELECT COUNT(e)
    FROM Employee e
    JOIN e.department d
    WHERE (
        :search IS NULL
        OR LOWER(CAST(e.firstName AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
        OR LOWER(CAST(e.lastName AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
        OR LOWER(CAST(e.email AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
    )
    AND (:status IS NULL OR e.status = :status)
    AND (:departmentId IS NULL OR d.id = :departmentId)
    """
    )
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("status") EmployeeStatus status,
            @Param("departmentId") UUID departmentId,
            Pageable pageable
    );


    // Para el Dashboard
    long countByStatus(EmployeeStatus status);

    @Query("""
SELECT e
FROM Employee e
JOIN FETCH e.department
""")
    Page<Employee> searchEmployees(Pageable pageable);

    long countByHireDateAfter(LocalDate date);

    @Query("SELECT e.hireDate FROM Employee e WHERE e.hireDate >= :from")
    List<LocalDate> findHireDatesFrom(@Param("from") LocalDate from);
}
