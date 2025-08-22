package com.financesoftware.payout.controller;

import com.financesoftware.common.enums.PayoutStatus;
import com.financesoftware.payout.dto.PayoutProcessRequest;
import com.financesoftware.payout.entity.Payout;
import com.financesoftware.payout.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payouts")
@Tag(name = "Payout API", description = "APIs for processing payouts and managing payout operations")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @PostMapping("/process")
    @Operation(summary = "Process a payout", description = "Processes a payout through the bank service, updating status and creating attempt history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payout processed successfully", content = @Content(schema = @Schema(implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payout data supplied", content = @Content(schema = @Schema(type = "string", example = "Invalid payout: missing required fields"))),
            @ApiResponse(responseCode = "500", description = "Internal server error during payout processing", content = @Content(schema = @Schema(type = "string", example = "Failed to process payout: Bank service unavailable")))
    })
    public ResponseEntity<?> processPayout(
            @Parameter(description = "Payout processing request containing payout details", required = true)
            @Valid @RequestBody PayoutProcessRequest request) {
        try {
            if (request.getPayout() == null) {
                return ResponseEntity.badRequest().body("Payout cannot be null");
            }

            Payout processedPayout = payoutService.processPayout(request.getPayout());
            return ResponseEntity.ok(processedPayout);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process payout: " + e.getMessage());
        }
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get payouts by status", description = "Returns payouts filtered by status. If no status is provided, returns all payouts", parameters = {
            @Parameter(name = "status", description = "Status of payouts to retrieve", required = false, schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT" }))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payouts by status", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getPayoutsByStatus(
            @Parameter(description = "Status of payouts to retrieve", required = false, example = "PAID", schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT" }))
            @RequestParam(name = "status", required = false) PayoutStatus status) {
        try {
            List<Payout> payouts = payoutService.getPayoutsByStatus(status);
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-merchant")
    @Operation(summary = "Get payouts by merchant", description = "Returns payouts for a specific merchant, optionally filtered by status", parameters = {
            @Parameter(name = "merchantId", description = "ID of the merchant", required = true),
            @Parameter(name = "status", description = "Status of payouts to retrieve", required = false, schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT" }))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant payouts", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid merchant ID supplied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getPayoutsByMerchantAndStatus(
            @Parameter(description = "ID of the merchant", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(name = "merchantId", required = true) UUID merchantId,
            @Parameter(description = "Status of payouts to retrieve", required = false, example = "PAID", schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "PROCESSING", "PAID", "FAILED", "CANCELLED", "INSUFFICIENT" }))
            @RequestParam(name = "status", required = false) PayoutStatus status) {
        try {
            List<Payout> payouts = payoutService.getPayoutsByMerchantAndStatus(merchantId, status);
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific payout details", description = "Returns details for a specific payout by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payout details", content = @Content(schema = @Schema(implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payout ID supplied"),
            @ApiResponse(responseCode = "404", description = "Payout not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Payout> getPayoutById(
            @Parameter(description = "ID of the payout", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID payoutId) {
        try {
            Payout payout = payoutService.getPayoutById(payoutId);
            if (payout == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(payout);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all payouts", description = "Returns all payouts in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all payouts", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getAllPayouts() {
        try {
            List<Payout> payouts = payoutService.getPayoutsByStatus(null);
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/fetch-ready-to-pay")
    @Operation(summary = "Fetch and process READY_TO_PAY payouts",
               description = "Manually fetches READY_TO_PAY payouts from Ledger Service and processes them. This provides a REST alternative to the RabbitMQ listener functionality.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched and processed READY_TO_PAY payouts",
                        content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error during fetching or processing",
                        content = @Content(schema = @Schema(type = "string", example = "Failed to fetch and process READY_TO_PAY payouts: Ledger service unavailable")))
    })
    public ResponseEntity<?> fetchAndProcessReadyToPayPayouts() {
        try {
            List<Payout> processedPayouts = payoutService.fetchAndProcessReadyToPayPayouts();
            return ResponseEntity.ok(processedPayouts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch and process READY_TO_PAY payouts: " + e.getMessage());
        }
    }
}
