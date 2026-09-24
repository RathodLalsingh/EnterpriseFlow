package com.opsflow.controller;

import com.opsflow.entity.AuditLog;
import com.opsflow.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<AuditLog>> getHistoryForRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(auditService.getHistoryForRequest(requestId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getHistoryForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getHistoryForUser(userId));
    }
}
