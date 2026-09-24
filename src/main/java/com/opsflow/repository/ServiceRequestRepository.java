package com.opsflow.repository;

import com.opsflow.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByRequestedBy(Long userId);
    List<ServiceRequest> findByStatus(String status);
}
