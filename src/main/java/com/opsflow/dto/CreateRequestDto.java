package com.opsflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Priority is required")
    private String priority;
}
