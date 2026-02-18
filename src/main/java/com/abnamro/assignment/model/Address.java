package com.abnamro.assignment.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @NotNull(message = "Street Name is required")
    private String streetName;

    @NotNull(message = "House Number is required")
    private String houseNumber;

    @NotNull(message = "Postcode is required")
    @Pattern(regexp = "^\\d{4}\\s?[a-zA-Z]{2}$", message = "Postcode must be 4 digits followed by 2 letters (e.g., 1000 AA)")
    private String postCode;

    @NotNull(message = "City is required")
    private String city;
}
