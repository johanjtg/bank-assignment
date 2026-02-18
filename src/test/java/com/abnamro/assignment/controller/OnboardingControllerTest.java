package com.abnamro.assignment.controller;

import com.abnamro.assignment.api.model.*;
import com.abnamro.assignment.service.OnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createApplication_ShouldReturn201() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        ApplicationResponse response = new ApplicationResponse();
        response.setId(id);
        response.setStatus(ApplicationStatus.DRAFT);

        //when
        when(service.createApplication(any(ApplicationCreateRequest.class))).thenReturn(response);

        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setName("Johan Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        Address address = new com.abnamro.assignment.api.model.Address();
        address.setStreetName("Kalverstraat");
        address.setHouseNumber("101");
        address.setPostCode("1234 AB");
        address.setCity("Amsterdam");
        request.setAddress(address);

        //then
        mockMvc.perform(post("/applications")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/applications/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updateApplication_ShouldValidateFormat() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        com.abnamro.assignment.api.model.Address address = new com.abnamro.assignment.api.model.Address();
        address.setPostCode("INVALID"); // Invalid format
        request.setAddress(address);

        // When/Then
        mockMvc.perform(patch("/applications/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.['address.postCode']")
                        .value("must match \"^\\d{4}\\s?[a-zA-Z]{2}$\""));
    }

    @Test
    void updateApplication_ShouldSucceed_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        com.abnamro.assignment.api.model.Address address = new com.abnamro.assignment.api.model.Address();
        address.setPostCode("1234 AB");
        request.setAddress(address);

        ApplicationResponse response = new ApplicationResponse();
        response.setId(id);
        com.abnamro.assignment.api.model.Address respAddress = new com.abnamro.assignment.api.model.Address();
        respAddress.setPostCode("1234 AB");
        response.setAddress(respAddress);

        when(service.updateApplication(eq(id), any(ApplicationUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/applications/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.postCode").value("1234 AB"));
    }

    @Test
    void submitApplication_ShouldReturn200() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        ApplicationResponse response = new ApplicationResponse();
        response.setId(id);
        response.setStatus(ApplicationStatus.COMPLETED);
        //when
        when(service.submitApplication(id)).thenReturn(response);
        //then
        mockMvc.perform(post("/applications/{id}/submit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void submitApplication_ShouldReturn409_WhenAlreadyCompleted() throws Exception {

        UUID id = UUID.randomUUID();
        //when
        when(service.submitApplication(id)).thenThrow(new IllegalStateException("Application is already completed"));
        //then
        mockMvc.perform(post("/applications/{id}/submit", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Application is already completed"));
    }
}
