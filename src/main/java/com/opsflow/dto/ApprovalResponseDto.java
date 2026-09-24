package com.opsflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponseDto {

    private Long id;
    private Long requestId;
    private Long approverId;
    private String approvalLevel;
    private String status;
    private String comments;
    private LocalDateTime approvedAt;
}
