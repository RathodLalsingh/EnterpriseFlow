package com.opsflow.controller;

import com.opsflow.dto.ApprovalRequestDto;
import com.opsflow.dto.ApprovalResponseDto;
import com.opsflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<ApprovalResponseDto>> getApprovalsForRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(approvalService.getApprovalsForRequest(requestId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovalResponseDto> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequestDto dto,
            Authentication authentication) {

        Long approverId = resolveApproverId(authentication);
        String comments = dto != null ? dto.getComments() : null;
        return ResponseEntity.ok(approvalService.approve(id, approverId, comments));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApprovalResponseDto> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequestDto dto,
            Authentication authentication) {

        Long approverId = resolveApproverId(authentication);
        String comments = dto != null ? dto.getComments() : null;
        return ResponseEntity.ok(approvalService.reject(id, approverId, comments));
    }

    private Long resolveApproverId(Authentication authentication) {
        return 2L;
    }
}
