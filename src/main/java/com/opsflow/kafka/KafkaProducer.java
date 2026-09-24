package com.opsflow.kafka;

import com.opsflow.entity.ServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRequestCreated(ServiceRequest request) {
        log.info("Publishing request.created for request id {}", request.getId());
        kafkaTemplate.send(KafkaTopicConfig.REQUEST_CREATED, request.getId().toString(), request);
    }
    public void publishRequestApproved(ServiceRequest request) {
        log.info("Publishing request.approved for request id {}", request.getId());
        kafkaTemplate.send(KafkaTopicConfig.REQUEST_APPROVED, request.getId().toString(), request);
    }
    public void publishRequestRejected(ServiceRequest request) {
        log.info("Publishing request.rejected for request id {}", request.getId());
        kafkaTemplate.send(KafkaTopicConfig.REQUEST_REJECTED, request.getId().toString(), request);
    }
    public void publishRequestCompleted(ServiceRequest request) {
        log.info("Publishing request.completed for request id {}", request.getId());
        kafkaTemplate.send(KafkaTopicConfig.REQUEST_COMPLETED, request.getId().toString(), request);
    }
}
