package com.opsflow.kafka;

import com.opsflow.entity.ServiceRequest;
import com.opsflow.service.ApprovalService;
import com.opsflow.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final ApprovalService approvalService;
    private final ExecutionService executionService;

    @KafkaListener(topics = KafkaTopicConfig.REQUEST_CREATED, groupId = "approval-group")
    public void consumeRequestCreated(ServiceRequest request) {
        log.info("Consumed request.created event for request {}", request.getId());
        approvalService.createApprovalWorkflow(request);
    }

    @KafkaListener(topics = KafkaTopicConfig.REQUEST_APPROVED, groupId = "execution-group")
    public void consumeRequestApproved(ServiceRequest request) {
        log.info("Consumed request.approved event for request {}", request.getId());
        executionService.executeRequest(request);
    }
}
