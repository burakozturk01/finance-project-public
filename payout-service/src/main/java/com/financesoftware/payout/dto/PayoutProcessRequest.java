package com.financesoftware.payout.dto;

import com.financesoftware.payout.entity.Payout;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for processing a payout")
public class PayoutProcessRequest {

    @Schema(description = "Payout to process", required = true)
    @NotNull(message = "Payout cannot be null")
    @Valid
    private Payout payout;

    // Default constructor
    public PayoutProcessRequest() {
    }

    // Constructor
    public PayoutProcessRequest(Payout payout) {
        this.payout = payout;
    }

    // Getter and Setter
    public Payout getPayout() {
        return payout;
    }

    public void setPayout(Payout payout) {
        this.payout = payout;
    }

    @Override
    public String toString() {
        return "PayoutProcessRequest{" +
                "payout=" + payout +
                '}';
    }
}
