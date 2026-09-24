package com.opsflow.service;

import com.opsflow.entity.AuditLog;
import com.opsflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId, Long requestId, String action, String description) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .requestId(requestId)
                .action(action)
                .description(description)
                .build();

        auditLogRepository.save(auditLog);
    }
    public List<AuditLog> getHistoryForRequest(Long requestId) {
        return auditLogRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
    }
    public List<AuditLog> getHistoryForUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
