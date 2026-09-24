package com.opsflow.repository;

import com.opsflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByRequestIdOrderByCreatedAtAsc(Long requestId);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
