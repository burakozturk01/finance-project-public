package com.financesoftware.ledger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Entity
@Table(name = "payout_transactions")
public class PayoutTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier for the payout-transaction relationship", required = true)
    private UUID id;

    @NotNull
    @Column(name = "payout_id", nullable = false)
    @Schema(description = "ID of the payout", required = true)
    private UUID payoutId;

    @NotNull
    @Column(name = "transaction_id", nullable = false)
    @Schema(description = "ID of the transaction", required = true)
    private UUID transactionId;

    // Constructors
    public PayoutTransaction() {}

    public PayoutTransaction(UUID payoutId, UUID transactionId) {
        this.payoutId = payoutId;
        this.transactionId = transactionId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(UUID payoutId) {
        this.payoutId = payoutId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
}
