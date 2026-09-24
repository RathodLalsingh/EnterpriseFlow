package com.opsflow.service;

import com.opsflow.dto.ApprovalResponseDto;
import com.opsflow.entity.Approval;
import com.opsflow.entity.ServiceRequest;
import com.opsflow.exception.ResourceNotFoundException;
import com.opsflow.kafka.KafkaProducer;
import com.opsflow.repository.ApprovalRepository;
import com.opsflow.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ServiceRequestRepository requestRepository;
    private final KafkaProducer kafkaProducer;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public void createApprovalWorkflow(ServiceRequest request) {

        Approval managerApproval = Approval.builder()
                .requestId(request.getId())
                .approvalLevel("MANAGER")
                .status("PENDING")
                .build();
        approvalRepository.save(managerApproval);

        Approval itApproval = Approval.builder()
                .requestId(request.getId())
                .approvalLevel("IT_ADMIN")
                .status("PENDING")
                .build();
        approvalRepository.save(itApproval);

        auditService.log(request.getRequestedBy(), request.getId(), "APPROVAL_REQUIRED",
                "Approval workflow created (MANAGER, IT_ADMIN)");

        notificationService.createNotification(
                request.getRequestedBy(),
                request.getId(),
                "Your request '" + request.getTitle() + "' is awaiting manager approval.",
                "INFO"
        );

        log.info("Approval workflow created for request {}", request.getId());
    }

    public List<ApprovalResponseDto> getApprovalsForRequest(Long requestId) {
        return approvalRepository.findByRequestId(requestId).stream()
                .map(this::convertToDto)
                .toList();
    }
    public ApprovalResponseDto approve(Long approvalId, Long approverId, String comments) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found with id: " + approvalId));

        approval.setStatus("APPROVED");
        approval.setApproverId(approverId);
        approval.setComments(comments);
        approval.setApprovedAt(LocalDateTime.now());
        Approval saved = approvalRepository.save(approval);

        ServiceRequest request = requestRepository.findById(approval.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found with id: " + approval.getRequestId()));

        auditService.log(approverId, request.getId(),
                approval.getApprovalLevel() + "_APPROVED",
                approval.getApprovalLevel() + " approved the request");

        notificationService.createNotification(
                request.getRequestedBy(),
                request.getId(),
                "Your request '" + request.getTitle() + "' was approved by " + approval.getApprovalLevel() + ".",
                "APPROVAL"
        );

        checkAndAdvanceRequest(request);

        return convertToDto(saved);
    }

    public ApprovalResponseDto reject(Long approvalId, Long approverId, String comments) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found with id: " + approvalId));

        approval.setStatus("REJECTED");
        approval.setApproverId(approverId);
        approval.setComments(comments);
        approval.setApprovedAt(LocalDateTime.now());
        Approval saved = approvalRepository.save(approval);

        ServiceRequest request = requestRepository.findById(approval.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found with id: " + approval.getRequestId()));

        request.setStatus("REJECTED");
        requestRepository.save(request);

        auditService.log(approverId, request.getId(),
                approval.getApprovalLevel() + "_REJECTED",
                approval.getApprovalLevel() + " rejected the request: " + comments);

        notificationService.createNotification(
                request.getRequestedBy(),
                request.getId(),
                "Your request '" + request.getTitle() + "' was rejected by " + approval.getApprovalLevel() + ".",
                "REJECTION"
        );

        kafkaProducer.publishRequestRejected(request);
        return convertToDto(saved);
    }
    private void checkAndAdvanceRequest(ServiceRequest request) {
        List<Approval> approvals = approvalRepository.findByRequestId(request.getId());

        boolean allApproved = approvals.stream()
                .allMatch(a -> "APPROVED".equals(a.getStatus()));

        if (allApproved) {
            request.setStatus("APPROVED");
            requestRepository.save(request);

            auditService.log(request.getRequestedBy(), request.getId(), "REQUEST_APPROVED",
                    "All approval levels approved the request");

            kafkaProducer.publishRequestApproved(request);
        }
    }

    private ApprovalResponseDto convertToDto(Approval approval) {
        return ApprovalResponseDto.builder()
                .id(approval.getId())
                .requestId(approval.getRequestId())
                .approverId(approval.getApproverId())
                .approvalLevel(approval.getApprovalLevel())
                .status(approval.getStatus())
                .comments(approval.getComments())
                .approvedAt(approval.getApprovedAt())
                .build();
    }
}
