package com.financesoftware.merchant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.annotations.CreationTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(hidden = true, required = true)
    private UUID id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    @Schema(description = "Full name of the merchant", required = true, example = "John Doe")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    @Schema(description = "Email of the merchant", required = true, example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "IBAN is required")
    @Column(nullable = false)
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$", message = "Invalid IBAN format")
    @Schema(description = "IBAN of the merchant", required = true, example = "TR12345678901234567890123456")
    private String iban;

    @DecimalMin("0.0")
    @Column(name = "debt", nullable = false, precision = 19, scale = 2)
    @Schema(description = "Current debt amount of the merchant", required = true, example = "0.00")
    private BigDecimal debt = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    @Schema(description = "Commission percentage for the merchant", required = true, example = "3.50")
    private BigDecimal commissionPercentage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(hidden = true, required = true)
    private LocalDateTime createdAt;

    // Constructors
    public Merchant() {}

    public Merchant(String name, String email, String iban, BigDecimal commissionPercentage) {
        this.name = name;
        this.email = email;
        this.iban = iban;
        this.commissionPercentage = commissionPercentage;
        this.debt = BigDecimal.ZERO;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public BigDecimal getDebt() {
        return debt;
    }

    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
