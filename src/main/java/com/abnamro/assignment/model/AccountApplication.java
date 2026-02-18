package com.abnamro.assignment.model;

import com.abnamro.assignment.model.validation.OnSubmit;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "account_applications")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Embedded
    private Address address;

    @NotNull(message = "Name is required", groups = OnSubmit.class)
    private String name;

    @NotNull(message = "Date of Birth is required", groups = OnSubmit.class)
    @Past(message = "Date of Birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "ID Document is required", groups = OnSubmit.class)
    private String idDocument;

    @NotNull(message = "Account Type is required", groups = OnSubmit.class)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @DecimalMin(value = "0.0", message = "Starting Balance must be positive")
    private BigDecimal startingBalance;

    @DecimalMin(value = "0.0", message = "Monthly Salary must be positive")
    private BigDecimal monthlySalary;

    private Boolean interestedInOtherProducts;

    @Email(message = "Email must be valid")
    private String email;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private java.time.Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private java.time.Instant updatedAt;
}
