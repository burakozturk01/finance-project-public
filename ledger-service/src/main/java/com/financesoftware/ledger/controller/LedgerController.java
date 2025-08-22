package com.financesoftware.ledger.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financesoftware.common.enums.PayoutStatus;
import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.ledger.entity.Payout;
import com.financesoftware.ledger.service.LedgerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ledgers")
@Tag(name = "Ledger API", description = "APIs for managing payouts and ledger operations")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;

    @PostMapping("/process-payouts")
    @Operation(summary = "Process payouts", description = "Manually triggers payout processing: fetch VALIDATED transactions, group by merchant, create payouts, update debts and transaction statuses, and publish READY_TO_PAY events. Note: In production, this runs once daily via scheduling.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payout processing completed successfully", content = @Content(schema = @Schema(type = "string", example = "Payout processing completed successfully"))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(type = "string", example = "Error processing payouts: Database connection failed")))
    })
    public ResponseEntity<String> processPayouts() {
        try {
            ledgerService.processPayouts();
            return ResponseEntity.ok("Payout processing completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing payouts: " + e.getMessage());
        }
    }

    @GetMapping("/payouts")
    @Operation(summary = "Get all payouts", description = "Returns all payouts in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all payouts", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getPayouts() {
        try {
            List<Payout> payouts = ledgerService.getPayouts();
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/payouts-by-status")
    @Operation(summary = "Get payouts by status", description = "Returns payouts filtered by status", parameters = {
            @Parameter(name = "status", description = "Status of payouts to retrieve", required = true, schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "INSUFFICIENT", "PAID", "CANCELLED" }))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payouts by status", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getPayoutsByStatus(
            @Parameter(description = "Status of payouts to retrieve", required = true, example = "NEW", schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "INSUFFICIENT", "PAID",
                    "CANCELLED" })) @RequestParam(name = "status", required = true) PayoutStatus status) {
        try {
            List<Payout> payouts = ledgerService.getPayoutsByStatus(status);
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/merchant-payouts")
    @Operation(summary = "Get payouts by merchant", description = "Returns payouts for a specific merchant, optionally filtered by status", parameters = {
            @Parameter(name = "merchantId", description = "ID of the merchant", required = true),
            @Parameter(name = "status", description = "Status of payouts to retrieve", required = false, schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "INSUFFICIENT", "PAID", "CANCELLED" }))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant payouts", content = @Content(schema = @Schema(type = "array", implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid merchant ID supplied"),
            @ApiResponse(responseCode = "404", description = "Merchant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payout>> getPayoutsByMerchant(
            @Parameter(description = "ID of the merchant", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @RequestParam(name = "merchantId", required = true) UUID merchantId,
            @Parameter(description = "Status of payouts to retrieve", required = false, example = "NEW", schema = @Schema(type = "string", allowableValues = {
                    "NEW", "READY_TO_PAY", "INSUFFICIENT", "PAID",
                    "CANCELLED" })) @RequestParam(name = "status", required = false) PayoutStatus status) {
        try {
            List<Payout> payouts;
            if (status == null)
                payouts = ledgerService.getPayoutsByMerchant(merchantId);
            else
                payouts = ledgerService.getPayoutsByMerchantAndStatus(merchantId, status);
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/payout")
    @Operation(summary = "Get specific payout details", description = "Returns details for a specific payout by ID", parameters = {
            @Parameter(name = "payoutId", description = "ID of the payout", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payout details", content = @Content(schema = @Schema(implementation = Payout.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payout ID supplied"),
            @ApiResponse(responseCode = "404", description = "Payout not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Payout> getPayoutById(
            @Parameter(description = "ID of the payout", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @RequestParam(name = "payoutId", required = true) UUID payoutId) {
        try {
            Payout payout = ledgerService.getPayoutById(payoutId);
            if (payout == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(payout);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/payout-transactions")
    @Operation(summary = "Get transactions for a payout", description = "Returns all transactions associated with a specific payout ID", parameters = {
            @Parameter(name = "payoutId", description = "ID of the payout", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions for payout", content = @Content(schema = @Schema(type = "array", implementation = com.financesoftware.ledger.entity.Transaction.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payout ID supplied"),
            @ApiResponse(responseCode = "404", description = "Payout not found or no transactions associated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<com.financesoftware.ledger.entity.Transaction>> getTransactionsForPayout(
            @Parameter(description = "ID of the payout", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @RequestParam(name = "payoutId", required = true) UUID payoutId) {
        try {
            List<com.financesoftware.ledger.entity.Transaction> transactions = ledgerService.getTransactionsForPayout(payoutId);
            if (transactions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/transactions/set-status")
    @Operation(summary = "Set transaction status", description = "Updates the status of multiple transactions to the specified status", parameters = {
            @Parameter(name = "transactionIds", description = "IDs of transactions to change statuses", required = true),
            @Parameter(name = "status", description = "TransactionStatus to change with", required = true)
    })

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions successfully updated", content = @Content(schema = @Schema(type = "string", example = "Successfully updated 5 transactions to PAID"))),
            @ApiResponse(responseCode = "400", description = "Invalid transaction IDs or status supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(type = "string", example = "Error updating transactions: Database connection failed")))
    })
    public ResponseEntity<String> setTransactionStatus(
            @Parameter(description = "List of transaction IDs to update", required = true) @RequestBody List<UUID> transactionIds,
            @Parameter(description = "New status for the transactions", required = true, example = "PAID") @RequestParam(name = "status", required = true) TransactionStatus status) {
        try {
            if (transactionIds == null || transactionIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Transaction IDs list cannot be empty");
            }

            int updatedCount = ledgerService.setTransactionStatus(transactionIds, status);
            return ResponseEntity.ok("Successfully updated " + updatedCount + " transactions to " + status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating transactions: " + e.getMessage());
        }
    }
}
