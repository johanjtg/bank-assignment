package com.abnamro.assignment.service;

import com.abnamro.assignment.api.model.ApplicationUpdateRequest;
import com.abnamro.assignment.model.AccountApplication;
import com.abnamro.assignment.model.AccountType;
import com.abnamro.assignment.model.ApplicationStatus;
import com.abnamro.assignment.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @Mock
    private ApplicationRepository repository;

    @Mock
    private org.springframework.validation.SmartValidator validator;

    private com.abnamro.assignment.mapper.ApplicationMapper mapper = new com.abnamro.assignment.mapper.ApplicationMapper();

    private OnboardingServiceImpl service;

    private AccountApplication app;
    private UUID appId;

    @BeforeEach
    void setUp() {
        service = new OnboardingServiceImpl(repository, mapper, validator);
        appId = UUID.randomUUID();
        app = new AccountApplication();
        app.setId(appId);
        app.setStatus(ApplicationStatus.DRAFT);
        app.setAddress(new com.abnamro.assignment.model.Address());
    }

    @Test
    void submitApplication_ShouldFail_WhenFieldsMissing() {
        // given
        when(repository.findById(appId)).thenReturn(Optional.of(app));

        doAnswer(invocation -> {
            org.springframework.validation.Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "required", "Name is required");
            return null;
        }).when(validator).validate(any(), any(), any(), any());

        // when
        Exception exception = assertThrows(com.abnamro.assignment.exception.ApplicationValidationException.class,
                () -> {
                    service.submitApplication(appId);
                });

        // then
        assertNotNull(exception);
    }

    @Test
    void submitApplication_ShouldSuccess_WhenValid() {
        // given
        app.setName("John Doe");
        com.abnamro.assignment.model.Address address = new com.abnamro.assignment.model.Address();
        address.setStreetName("Main St");
        address.setHouseNumber("1");
        address.setPostCode("1000 AA");
        address.setCity("Amsterdam");
        app.setAddress(address);
        app.setDateOfBirth(LocalDate.of(1990, 1, 1));
        app.setIdDocument("ID123");
        app.setAccountType(AccountType.SAVINGS);

        when(repository.findById(appId)).thenReturn(Optional.of(app));
        when(repository.save(any(AccountApplication.class))).thenReturn(app);

        // when
        var response = service.submitApplication(appId);

        // then
        assertEquals(com.abnamro.assignment.api.model.ApplicationStatus.COMPLETED, response.getStatus());
    }

    @Test
    void updateApplication_ShouldFail_WhenAlreadyCompleted() {
        app.setStatus(ApplicationStatus.COMPLETED);
        when(repository.findById(appId)).thenReturn(Optional.of(app));

        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        request.setName("New Name");

        assertThrows(IllegalStateException.class, () -> {
            service.updateApplication(appId, request);
        });
    }

    @Test
    void submitApplication_ShouldFail_WhenAlreadyCompleted() {
        app.setStatus(ApplicationStatus.COMPLETED);
        when(repository.findById(appId)).thenReturn(Optional.of(app));

        assertThrows(IllegalStateException.class, () -> {
            service.submitApplication(appId);
        });
    }

    @Test
    void createApplication_ShouldSuccess_WhenValid() {
        // given
        com.abnamro.assignment.api.model.ApplicationCreateRequest request = new com.abnamro.assignment.api.model.ApplicationCreateRequest();
        request.setName("Jane Doe");
        request.setDateOfBirth(LocalDate.of(1995, 5, 15));

        when(repository.save(any(AccountApplication.class))).thenAnswer(invocation -> {
            AccountApplication savedApp = invocation.getArgument(0);
            savedApp.setId(UUID.randomUUID());
            return savedApp;
        });

        // when
        var response = service.createApplication(request);

        // then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals(com.abnamro.assignment.api.model.ApplicationStatus.DRAFT, response.getStatus());
    }

    @Test
    void getApplication_ShouldReturnApp_WhenFound() {
        when(repository.findById(appId)).thenReturn(Optional.of(app));

        var response = service.getApplication(appId);

        assertNotNull(response);
        assertEquals(appId, response.getId());
    }

    @Test
    void getApplication_ShouldThrow_WhenNotFound() {
        when(repository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(com.abnamro.assignment.exception.ResourceNotFoundException.class, () -> {
            service.getApplication(appId);
        });
    }
}
