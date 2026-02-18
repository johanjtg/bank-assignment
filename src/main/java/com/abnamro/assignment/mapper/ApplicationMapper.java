package com.abnamro.assignment.mapper;

import com.abnamro.assignment.api.model.ApplicationCreateRequest;
import com.abnamro.assignment.api.model.ApplicationResponse;
import com.abnamro.assignment.api.model.ApplicationUpdateRequest;
import com.abnamro.assignment.model.AccountApplication;
import com.abnamro.assignment.model.AccountType;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(AccountApplication entity) {
        if (entity == null) {
            return null;
        }

        ApplicationResponse response = new ApplicationResponse();
        response.setId(entity.getId());
        response.setStatus(toApiStatus(entity.getStatus()));
        response.setName(entity.getName());
        response.setAddress(toApiAddress(entity.getAddress()));
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setIdDocument(entity.getIdDocument());
        response.setAccountType(toApiAccountType(entity.getAccountType()));
        response.setStartingBalance(entity.getStartingBalance());
        response.setMonthlySalary(entity.getMonthlySalary());
        response.setInterestedInOtherProducts(entity.getInterestedInOtherProducts());
        response.setEmail(entity.getEmail());

        if (entity.getCreatedAt() != null) {
            response.setCreatedAt(entity.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toOffsetDateTime());
        }
        if (entity.getUpdatedAt() != null) {
            response.setUpdatedAt(entity.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toOffsetDateTime());
        }

        return response;
    }

    public void updateFromRequest(AccountApplication application, ApplicationCreateRequest request) {
        if (request == null) {
            return;
        }

        if (request.getName() != null)
            application.setName(request.getName());
        if (request.getDateOfBirth() != null)
            application.setDateOfBirth(request.getDateOfBirth());
        if (request.getIdDocument() != null)
            application.setIdDocument(request.getIdDocument());
        if (request.getEmail() != null)
            application.setEmail(request.getEmail());

        if (request.getAccountType() != null) {
            application.setAccountType(toDomainAccountType(request.getAccountType()));
        }

        if (request.getAddress() != null) {
            updateDomainAddress(application, request.getAddress());
        }
    }

    public void updateFromRequest(AccountApplication application, ApplicationUpdateRequest request) {
        if (request == null) {
            return;
        }

        if (request.getName() != null)
            application.setName(request.getName());
        if (request.getDateOfBirth() != null)
            application.setDateOfBirth(request.getDateOfBirth());
        if (request.getIdDocument() != null)
            application.setIdDocument(request.getIdDocument());
        if (request.getEmail() != null)
            application.setEmail(request.getEmail());

        if (request.getAccountType() != null) {
            application.setAccountType(toDomainAccountType(request.getAccountType()));
        }

        if (request.getStartingBalance() != null)
            application.setStartingBalance(request.getStartingBalance());
        if (request.getMonthlySalary() != null)
            application.setMonthlySalary(request.getMonthlySalary());
        if (request.getInterestedInOtherProducts() != null)
            application.setInterestedInOtherProducts(request.getInterestedInOtherProducts());

        if (request.getAddress() != null) {
            updateDomainAddress(application, request.getAddress());
        }
    }

    // Helper Methods

    private com.abnamro.assignment.api.model.ApplicationStatus toApiStatus(
            com.abnamro.assignment.model.ApplicationStatus status) {
        if (status == null)
            return null;
        try {
            return com.abnamro.assignment.api.model.ApplicationStatus.valueOf(status.name());
        } catch (IllegalArgumentException e) {
            return null; // Or handle as error
        }
    }

    private com.abnamro.assignment.api.model.AccountType toApiAccountType(AccountType type) {
        if (type == null)
            return null;
        try {
            return com.abnamro.assignment.api.model.AccountType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private AccountType toDomainAccountType(com.abnamro.assignment.api.model.AccountType type) {
        if (type == null)
            return null;
        try {
            return AccountType.valueOf(type.getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private com.abnamro.assignment.api.model.Address toApiAddress(com.abnamro.assignment.model.Address address) {
        if (address == null)
            return null;
        com.abnamro.assignment.api.model.Address apiAddress = new com.abnamro.assignment.api.model.Address();
        apiAddress.setStreetName(address.getStreetName());
        apiAddress.setHouseNumber(address.getHouseNumber());
        apiAddress.setPostCode(address.getPostCode());
        apiAddress.setCity(address.getCity());
        return apiAddress;
    }

    private void updateDomainAddress(AccountApplication application,
            com.abnamro.assignment.api.model.Address apiAddress) {
        if (application.getAddress() == null) {
            application.setAddress(new com.abnamro.assignment.model.Address());
        }
        if (apiAddress.getStreetName() != null)
            application.getAddress().setStreetName(apiAddress.getStreetName());
        if (apiAddress.getHouseNumber() != null)
            application.getAddress().setHouseNumber(apiAddress.getHouseNumber());
        if (apiAddress.getPostCode() != null)
            application.getAddress().setPostCode(apiAddress.getPostCode());
        if (apiAddress.getCity() != null)
            application.getAddress().setCity(apiAddress.getCity());
    }
}
