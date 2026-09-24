package com.opsflow.service;

import com.opsflow.dto.CreateRequestDto;
import com.opsflow.dto.RequestResponseDto;
import com.opsflow.entity.ServiceRequest;
import com.opsflow.exception.ResourceNotFoundException;
import com.opsflow.kafka.KafkaProducer;
import com.opsflow.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository requestRepository;
    private final KafkaProducer kafkaProducer;
    private final AuditService auditService;

    public RequestResponseDto createRequest(CreateRequestDto dto, Long userId) {
        ServiceRequest request = new ServiceRequest();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setType(dto.getType());
        request.setPriority(dto.getPriority());
        request.setRequestedBy(userId);
        request.setStatus("PENDING_APPROVAL");

        ServiceRequest saved = requestRepository.save(request);
        auditService.log(userId, saved.getId(), "REQUEST_CREATED",
                "Request created: " + saved.getTitle());

        kafkaProducer.publishRequestCreated(saved);
        return convertToDto(saved);
    }

    @Cacheable(value = "requests", key = "#id")
    public RequestResponseDto getRequestById(Long id) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return convertToDto(request);
    }

    public List<RequestResponseDto> getRequestsForUser(Long userId) {
        return requestRepository.findByRequestedBy(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<RequestResponseDto> getRequestsByStatus(String status) {
        return requestRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }
    @CacheEvict(value = "requests", key = "#requestId")
    public ServiceRequest updateStatus(Long requestId, String status) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
        request.setStatus(status);
        return requestRepository.save(request);
    }
    private RequestResponseDto convertToDto(ServiceRequest request) {
        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .status(request.getStatus())
                .requestedBy(request.getRequestedBy())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
