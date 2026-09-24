package com.opsflow.controller;

import com.opsflow.dto.CreateRequestDto;
import com.opsflow.dto.RequestResponseDto;
import com.opsflow.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<RequestResponseDto> createRequest(
            @Valid @RequestBody CreateRequestDto dto,
            Authentication authentication) {

        Long userId = resolveUserId(authentication);
        RequestResponseDto response = requestService.createRequest(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RequestResponseDto>> getMyRequests(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(requestService.getRequestsForUser(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RequestResponseDto>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }

    @GetMapping
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    private Long resolveUserId(Authentication authentication) {
        return 1L;
    }
}
