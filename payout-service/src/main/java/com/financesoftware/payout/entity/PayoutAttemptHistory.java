package com.financesoftware.payout.entity;

import com.financesoftware.common.enums.PayoutStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to store the history of payout attempts
 */
@Entity
@Table(name = "payout_attempt_history")
@Schema(description = "Entity representing the history of payout attempts")
public class PayoutAttemptHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier for the payout attempt", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id", nullable = false)
    @Schema(description = "The payout associated with this attempt")
    private Payout payout;

    @Column(name = "attempt_timestamp", nullable = false)
    @Schema(description = "Timestamp when the payout attempt was made", required = true, example = "2023-12-01T10:30:00")
    private LocalDateTime attemptTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Status result of the payout attempt", required = true, allowableValues = {"NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT"})
    private PayoutStatus status;

    @Column(name = "reason")
    @Schema(description = "Reason for the attempt result", example = "Payment successful")
    private String reason;

    // Default constructor
    public PayoutAttemptHistory() {
    }

    // Constructor
    public PayoutAttemptHistory(Payout payout, LocalDateTime attemptTimestamp, PayoutStatus status, String reason) {
        this.payout = payout;
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

    public Payout getPayout() {
        return payout;
    }

    public void setPayout(Payout payout) {
        this.payout = payout;
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
        return "PayoutAttemptHistory{" +
                "id=" + id +
                ", attemptTimestamp=" + attemptTimestamp +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
