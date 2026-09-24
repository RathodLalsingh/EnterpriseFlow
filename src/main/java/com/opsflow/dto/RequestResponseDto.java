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
public class RequestResponseDto {

    private Long id;

    private String title;
    private String description;
    private String type;
    private String priority;
    private String status;
    private Long requestedBy;

    private LocalDateTime createdAt;
}
