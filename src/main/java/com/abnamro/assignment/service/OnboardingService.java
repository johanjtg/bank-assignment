package com.abnamro.assignment.service;

import com.abnamro.assignment.api.model.ApplicationCreateRequest;
import com.abnamro.assignment.api.model.ApplicationResponse;
import com.abnamro.assignment.api.model.ApplicationUpdateRequest;

import java.util.UUID;

public interface OnboardingService {
    ApplicationResponse createApplication(ApplicationCreateRequest request);

    ApplicationResponse getApplication(UUID id);

    ApplicationResponse updateApplication(UUID id, ApplicationUpdateRequest request);

    ApplicationResponse submitApplication(UUID id);
}
