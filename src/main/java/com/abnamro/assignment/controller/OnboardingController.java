package com.abnamro.assignment.controller;

import com.abnamro.assignment.api.ApplicationsApi;
import com.abnamro.assignment.api.model.ApplicationCreateRequest;
import com.abnamro.assignment.api.model.ApplicationResponse;
import com.abnamro.assignment.api.model.ApplicationUpdateRequest;
import com.abnamro.assignment.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OnboardingController implements ApplicationsApi {

    private final OnboardingService service;

    @Override
    public ResponseEntity<ApplicationResponse> createApplication(ApplicationCreateRequest request) {
        ApplicationResponse response = service.createApplication(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<ApplicationResponse> getApplication(UUID id) {
        return ResponseEntity.ok(service.getApplication(id));
    }

    @Override
    public ResponseEntity<ApplicationResponse> updateApplication(UUID id, ApplicationUpdateRequest request) {
        return ResponseEntity.ok(service.updateApplication(id, request));
    }

    @Override
    public ResponseEntity<ApplicationResponse> submitApplication(UUID id) {
        return ResponseEntity.ok(service.submitApplication(id));
    }
}
