package com.opsflow.repository;

import com.opsflow.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByRequestId(Long requestId);

}
