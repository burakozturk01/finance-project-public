package com.financesoftware.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.financesoftware.common.enums.TransactionStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(hidden = true, required = true)
    private UUID id;

    @NotNull
    @Column(name = "merchant_id", nullable = false)
    @Schema(description = "ID of the merchant", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID merchantId;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false)
    @Schema(description = "Amount of the transaction", required = true, example = "100.00")
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Currency of the transaction", required = true, example = "USD")
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "card_scheme", nullable = false)
    private CardScheme cardScheme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(hidden = true)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Creation timestamp of the transaction", required = true, hidden = true)
    private LocalDateTime createdAt;

    public enum CardScheme {
        VISA, MASTERCARD, TROY
    }

    // Constructors
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.status = TransactionStatus.RECEIVED;
    }

    public Transaction(UUID merchantId, BigDecimal amount, String currency, CardScheme cardScheme) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.cardScheme = cardScheme;
        this.status = TransactionStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CardScheme getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(CardScheme cardScheme) {
        this.cardScheme = cardScheme;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
