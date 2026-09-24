package com.opsflow.service;

import com.opsflow.entity.ServiceRequest;
import com.opsflow.kafka.KafkaProducer;
import com.opsflow.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final ServiceRequestRepository requestRepository;
    private final KafkaProducer kafkaProducer;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public void executeRequest(ServiceRequest request) {

        request.setStatus("PROCESSING");
        requestRepository.save(request);
        auditService.log(request.getRequestedBy(), request.getId(), "EXECUTION_STARTED",
                "Provisioning started for: " + request.getType());

        log.info("Executing request {} of type {}", request.getId(), request.getType());

        request.setStatus("COMPLETED");
        ServiceRequest completed = requestRepository.save(request);

        auditService.log(request.getRequestedBy(), request.getId(), "REQUEST_COMPLETED",
                "Access provisioned and request marked complete");

        notificationService.createNotification(
                request.getRequestedBy(),
                request.getId(),
                "Your request '" + request.getTitle() + "' has been completed.",
                "COMPLETION"
        );
        kafkaProducer.publishRequestCompleted(completed);
    }
}
