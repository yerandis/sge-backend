package com.yerandis.sge.repository.system;

import com.yerandis.sge.entity.admin.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Historial de cambios de una entidad específica.
     * Se ordena del más reciente al más antiguo.
     *
     * Spring Data genera:
     * SELECT * FROM audit_logs
     * WHERE entity_type = ? AND entity_id = ?
     * ORDER BY created_at DESC
     */
    List<com.yerandis.sge.entity.admin.AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId
    );

    /**
     * Todos los logs de un usuario.
     */
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
  }
