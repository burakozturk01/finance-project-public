package com.financesoftware.payout.dto;

import com.financesoftware.common.enums.PayoutStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Data transfer object for payout attempt history")
public class PayoutAttemptHistoryDTO {

    @Schema(description = "Unique identifier for the attempt", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Timestamp of the attempt", required = true, example = "2023-12-01T10:30:00")
    private LocalDateTime attemptTimestamp;

    @Schema(description = "Status result of the attempt", required = true, allowableValues = {"NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT"})
    private PayoutStatus status;

    @Schema(description = "Reason for the attempt result", example = "Payment successful")
    private String reason;

    // Default constructor
    public PayoutAttemptHistoryDTO() {
    }

    // Constructor
    public PayoutAttemptHistoryDTO(UUID id, LocalDateTime attemptTimestamp, PayoutStatus status, String reason) {
        this.id = id;
        this.attemptTimestamp = attemptTimestamp;
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getAttemptTimestamp() {
        return attemptTimestamp;
    }

    public void setAttemptTimestamp(LocalDateTime attemptTimestamp) {
        this.attemptTimestamp = attemptTimestamp;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "PayoutAttemptHistoryDTO{" +
                "id=" + id +
                ", attemptTimestamp=" + attemptTimestamp +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
