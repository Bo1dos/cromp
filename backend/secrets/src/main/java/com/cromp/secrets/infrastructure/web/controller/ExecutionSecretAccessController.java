package com.cromp.secrets.infrastructure.web.controller;

import com.cromp.secrets.api.dto.request.RecordSecretAccessRequest;
import com.cromp.secrets.application.service.ExecutionSecretAccessApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/execution-secret-access")
@RequiredArgsConstructor
public class ExecutionSecretAccessController {
    private final ExecutionSecretAccessApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void record(@Valid @RequestBody RecordSecretAccessRequest request) {
        service.recordAccess(request);
    }
}
