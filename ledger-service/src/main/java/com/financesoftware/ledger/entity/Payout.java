package com.financesoftware.ledger.entity;

import com.financesoftware.common.enums.PayoutStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier for the payout", required = true)
    private UUID id;

    @NotNull
    @Column(name = "merchant_id", nullable = false)
    @Schema(description = "ID of the merchant receiving the payout", required = true)
    private UUID merchantId;

    @Column(name = "gross_amount", precision = 19, scale = 2)
    @Schema(description = "Total amount before deductions", example = "1000.00")
    private BigDecimal grossAmount;

    @NotNull // Commission rate will be known from the start
    @Column(name = "commission_rate", precision = 19, scale = 2)
    @Schema(description = "Commission rate deducted", example = "3.50")
    private BigDecimal commissionRate;

    @Column(name = "debt_amount", precision = 19, scale = 2)
    @Schema(description = "Debt amount deducted", example = "120.30")
    private BigDecimal debtAmount;

    @Column(name = "net_amount", precision = 19, scale = 2)
    @Schema(description = "Final payout amount after all deductions", example = "850.00")
    private BigDecimal netAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Status of the payout", required = true)
    private PayoutStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(hidden = true, required = true)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    @Schema(description = "Processing timestamp of the payout")
    private LocalDateTime processedAt;

    // Constructors
    public Payout() {}

    public Payout(UUID merchantId, BigDecimal commissionRate) {
        this.merchantId = merchantId;
        this.commissionRate = commissionRate;
        this.status = PayoutStatus.NEW;
        if (status == PayoutStatus.READY_TO_PAY) {
            this.processedAt = LocalDateTime.now();
        }
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

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(BigDecimal debtAmount) {
        this.debtAmount = debtAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
        if (status == PayoutStatus.READY_TO_PAY && this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
