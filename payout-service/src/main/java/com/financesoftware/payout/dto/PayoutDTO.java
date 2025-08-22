package com.financesoftware.payout.dto;

import com.financesoftware.common.enums.PayoutStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PayoutDTO {

    @Schema(description = "Unique identifier for the payout", required = true)
    private UUID id;

    @NotNull
    @Schema(description = "ID of the merchant receiving the payout", required = true)
    private UUID merchantId;

    @Schema(description = "Total amount before deductions", example = "1000.00")
    private BigDecimal grossAmount;

    @NotNull
    @Schema(description = "Commission rate deducted", example = "3.50")
    private BigDecimal commissionRate;

    @Schema(description = "Debt amount deducted", example = "120.30")
    private BigDecimal debtAmount;

    @Schema(description = "Final payout amount after all deductions", example = "850.00")
    private BigDecimal netAmount;

    @NotNull
    @Schema(description = "Status of the payout", required = true)
    private PayoutStatus status;

    @Schema(hidden = true, required = true)
    private LocalDateTime createdAt;

    @Schema(description = "Processing timestamp of the payout")
    private LocalDateTime processedAt;

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
