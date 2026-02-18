package com.abnamro.assignment.service;

import com.abnamro.assignment.api.model.ApplicationCreateRequest;
import com.abnamro.assignment.api.model.ApplicationResponse;
import com.abnamro.assignment.api.model.ApplicationUpdateRequest;
import com.abnamro.assignment.exception.ApplicationValidationException;
import com.abnamro.assignment.exception.ResourceNotFoundException;
import com.abnamro.assignment.mapper.ApplicationMapper;
import com.abnamro.assignment.model.AccountApplication;
import com.abnamro.assignment.model.ApplicationStatus;
import com.abnamro.assignment.model.validation.OnSubmit;
import com.abnamro.assignment.repository.ApplicationRepository;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Service for managing the onboarding lifecycle of bank account applications.
 * Handles creation, updates, retrieval, and final submission of applications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;
    private final SmartValidator validator;

    /**
     * Creates a new account application in DRAFT status.
     *
     * @param request The initial application details (optional).
     * @return The created application response.
     */
    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request) {
        log.info("Creating new application");
        AccountApplication application = new AccountApplication();
        application.setStatus(ApplicationStatus.DRAFT);

        // Map fields from request
        if (request != null) {
            mapper.updateFromRequest(application, request);
        }

        AccountApplication saved = repository.save(application);
        log.info("Created application with ID: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    /**
     * Retrieves an application by its ID.
     *
     * @param id The unique identifier of the application.
     * @return The application response.
     * @throws ResourceNotFoundException if the application is not found.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    /**
     * Updates an existing application with new details.
     * Validates the allowed fields and updates the entity.
     *
     * @param id      The unique identifier of the application.
     * @param request The partial update request containing fields to change.
     * @return The updated application response.
     * @throws ResourceNotFoundException if the application is not found.
     * @throws IllegalStateException     if the application is already COMPLETED.
     */
    @Transactional
    public ApplicationResponse updateApplication(UUID id, ApplicationUpdateRequest request) {
        log.info("Updating application with ID: {}", id);
        AccountApplication application = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Application not found with ID: {}", id);
                    return new ResourceNotFoundException("Application not found");
                });

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            log.error("Cannot update completed application with ID: {}", id);
            throw new IllegalStateException("Application is already completed and cannot be updated");
        }

        mapper.updateFromRequest(application, request);

        AccountApplication saved = repository.save(application);
        log.info("Updated application with ID: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    /**
     * Submits an application for final processing.
     * Performs comprehensive validation of all mandatory fields.
     *
     * @param id The unique identifier of the application.
     * @return The submitted application response with status COMPLETED.
     * @throws IllegalArgumentException if the application is not found.
     * @throws IllegalStateException    if the application is already COMPLETED.
     * @throws ResponseStatusException  if validation fails.
     */
    public ApplicationResponse submitApplication(UUID id) {
        log.info("Submitting application with ID: {}", id);
        AccountApplication application = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Application not found with ID: {}", id);
                    return new IllegalArgumentException("Application not found");
                });

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            log.error("Cannot submit completed application with ID: {}", id);
            throw new IllegalStateException("Application is already completed");
        }

        // Validate the entity state
        BindingResult errors = new BeanPropertyBindingResult(application,
                "accountApplication");
        validator.validate(application, errors, Default.class, OnSubmit.class);

        if (errors.hasErrors()) {
            log.error("Validation failed for application ID: {}. Errors: {}", id, errors.getAllErrors());
            throw new ApplicationValidationException(errors);
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        AccountApplication saved = repository.save(application);
        log.info("Submitted application with ID: {}", id);
        return mapper.toResponse(saved);
    }
}
