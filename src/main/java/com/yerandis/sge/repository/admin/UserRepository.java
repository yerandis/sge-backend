package com.yerandis.sge.repository.admin;


import com.yerandis.sge.entity.admin.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    /**
     * Búsqueda de usuarios con filtro opcional por username.
     *
     * CORRECCIONES:
     * 1. Eliminado el parámetro 'username' que no se usaba en la query
     * 2. Eliminado el CAST innecesario — JPQL no necesita castear strings
     * 3. Query simplificada y correcta en JPQL estándar
     *
     * La cláusula (:search IS NULL OR ...) permite pasar null para obtener todos.
     */
    @Query(
            value = """
            SELECT u FROM User u
            WHERE (:search IS NULL
                OR LOWER(CAST(u.username AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%'))
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:search IS NULL
                OR LOWER(CAST(u.username AS text)) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%'))
            """
    )
    Page<User> searchUser(
            @Param("search") String search,
            Pageable pageable          // ← eliminado @Param("username") que no se usaba
    );

    boolean existsByEmail(String email);
}
