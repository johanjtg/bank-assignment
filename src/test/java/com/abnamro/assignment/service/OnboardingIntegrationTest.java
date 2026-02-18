package com.abnamro.assignment.service;

import com.abnamro.assignment.api.model.ApplicationCreateRequest;
import com.abnamro.assignment.api.model.ApplicationResponse;
import com.abnamro.assignment.exception.ApplicationValidationException;
import com.abnamro.assignment.model.AccountApplication;
import com.abnamro.assignment.model.AccountType;
import com.abnamro.assignment.model.Address;
import com.abnamro.assignment.model.ApplicationStatus;
import com.abnamro.assignment.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OnboardingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
    }

    @Test
    void submitApplication_ShouldFailValidation_WhenDataIsInvalid() {
        // Given
        AccountApplication app = new AccountApplication();
        app.setStatus(ApplicationStatus.DRAFT);
        app.setName(null);
        app.setDateOfBirth(null);

        AccountApplication savedApp = applicationRepository.save(app);
        UUID appId = savedApp.getId();

        // When/Then
        assertThrows(ApplicationValidationException.class, () -> {
            onboardingService.submitApplication(appId);
        });

        // Status is still DRAFT
        AccountApplication storedApp = applicationRepository.findById(appId).orElseThrow();
        assertEquals(ApplicationStatus.DRAFT, storedApp.getStatus());
    }

    @Test
    void submitApplication_ShouldSucceed_WhenDataIsValid() {
        //Given
        AccountApplication app = new AccountApplication();
        app.setStatus(ApplicationStatus.DRAFT);
        app.setName("Integration Test User");
        app.setDateOfBirth(LocalDate.of(1990, 1, 1));
        app.setAccountType(AccountType.SAVINGS);
        app.setIdDocument("SE12345678");

        Address address = new Address();
        address.setStreetName("Main St");
        address.setHouseNumber("1");
        address.setPostCode("1234 AB");
        address.setCity("Amsterdam");
        app.setAddress(address);

        AccountApplication savedApp = applicationRepository.save(app);
        UUID appId = savedApp.getId();

        // When
        ApplicationResponse response = onboardingService.submitApplication(appId);

        // Then
        assertEquals(com.abnamro.assignment.api.model.ApplicationStatus.COMPLETED, response.getStatus());

        AccountApplication storedApp = applicationRepository.findById(appId).orElseThrow();
        assertEquals(ApplicationStatus.COMPLETED, storedApp.getStatus());
    }

    @Test
    void createApplication_ShouldFail_WhenDateOfBirthIsInFuture() {
        //given
        AccountApplication app = new AccountApplication();
        app.setStatus(ApplicationStatus.DRAFT);
        app.setDateOfBirth(LocalDate.now().plusDays(1)); // Invalid

        // then
        assertThrows(TransactionSystemException.class, () -> {
            applicationRepository.save(app);
            applicationRepository.flush();
        });
    }

    @Test
    void createApplicationViaApi_ShouldReturn400_WhenDateOfBirthIsInFuture() throws Exception {
        // given
        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setName("API Test User");
        request.setDateOfBirth(LocalDate.now().plusDays(1)); // Future date
        request.setAddress(new com.abnamro.assignment.api.model.Address()); // Empty address ok for Draft? Address
                                                                            // fields are optional in DTO?

        com.abnamro.assignment.api.model.Address address = new com.abnamro.assignment.api.model.Address();
        address.setStreetName("Main St");
        address.setHouseNumber("1");
        address.setPostCode("1234 AB");
        address.setCity("Amsterdam");
        request.setAddress(address);

        // when/then
        mockMvc.perform(post("/applications")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.dateOfBirth").exists()); // Check that our custom handler mapped it
    }

    @Test
    void createApplicationViaApi_ShouldReturn400_WhenMandatoryFieldsAreMissing() throws Exception {
        // given
        ApplicationCreateRequest request = new ApplicationCreateRequest();

        // when/then
        mockMvc.perform(post("/applications")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").exists()) // Name is mandatory
                .andExpect(jsonPath("$.errors.address").exists()) // Address is mandatory
                .andExpect(jsonPath("$.errors.dateOfBirth").exists()); // DOB is mandatory
    }

    @Test
    void createApplicationViaApi_ShouldReturn400_WhenJsonIsMalformed() throws Exception {
        //given
        String malformedJson = "{ \"name\": \"John\" ";

        //when/then
        mockMvc.perform(post("/applications")
                .contentType("application/json")
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed JSON request"));
    }

    @Test
    void submitApplication_ShouldReturn400_WithErrors_WhenMandatoryFieldsAreMissing() throws Exception {
        // given
        AccountApplication app = new AccountApplication();
        app.setStatus(ApplicationStatus.DRAFT);
        app.setName("John Doe");
        app.setDateOfBirth(LocalDate.of(1990, 1, 1));

        AccountApplication savedApp = applicationRepository.save(app);
        UUID appId = savedApp.getId();

        //when / then
        mockMvc.perform(post("/applications/" + appId + "/submit")
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.accountType").exists())
                .andExpect(jsonPath("$.errors.idDocument").exists()); // Should exist
    }
}
