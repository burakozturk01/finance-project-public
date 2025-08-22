package com.financesoftware.transaction.controller;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financesoftware.transaction.entity.Transaction;
import com.financesoftware.transaction.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction API", description = "APIs for managing transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/submit")
    @Operation(summary = "Submit a new transaction", description = "Submits a new transaction for processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Transaction already exists")
    })
    public ResponseEntity<UUID> submitTransaction(
        @Parameter(description = "Transaction object that needs to be created.", required = true)
        @Valid @RequestBody Transaction transaction) {
        Transaction processedTransaction = transactionService.processTransaction(transaction);
        return new ResponseEntity<>(processedTransaction.getId(), HttpStatus.CREATED);
    }

    @GetMapping()
    @Operation(summary = "Get all transactions", description = "Returns all transactions made.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all transactions",
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/get-by-status")
    @Operation(summary = "Get transactions by status with pagination", description = "Returns transactions filtered by status with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Transaction>> getTransactionsByStatus(
            @Parameter(description = "Status of transactions to retrieve", required = true)
            @RequestParam("status") String status,
            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(value = "size", defaultValue = "1000") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getTransactionsByStatus(status, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/get")
    @Operation(parameters = {
        @Parameter(name = "id", description = "ID of the transaction to be retrieved", required = true)},
        summary = "Get a transaction by id", description = "Returns a transaction by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a transaction",
                    content = @Content(schema = @Schema(implementation = Transaction.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> getTransactionById(
            @RequestParam("id") String id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a transaction by id", description = "Deletes a transaction by id.",
    parameters = {
            @Parameter(name = "id", description = "ID of the transaction to be deleted", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteTransaction(
            @RequestParam("id") String id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        if (transaction.isPresent()) {
            transactionService.deleteTransaction(transaction.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/update-status")
    @Operation(summary = "Update the status of a transaction by id", description = "Updates the status of a transaction by id.",
    parameters = {
            @Parameter(name = "id", description = "ID of the transaction to be updated", required = true),
            @Parameter(name = "status", description = "New status of the transaction to be updated", required = true,
                       schema = @Schema(type = "string", allowableValues = {"RECEIVED", "PENDING", "VALIDATED", "PAID", "FAILED"}))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden new status"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UUID> updateTransactionStatus(
            @RequestParam("id") String id, @RequestParam("status") String status) {
        Optional<UUID> updatedTransactionId = transactionService.updateTransactionStatus(id, status);
        if (updatedTransactionId.isPresent()) {
            return ResponseEntity.ok(updatedTransactionId.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // New: Align with Feign client - PUT /api/transactions/{id}/status
    @PutMapping("/{id}/status")
    @Operation(summary = "Update transaction status (path)", description = "Updates the status of a transaction using a path variable ID and status as request param",
        parameters = {
            @Parameter(name = "id", description = "Transaction ID", required = true),
            @Parameter(name = "status", description = "New status", required = true,
                schema = @Schema(type = "string", allowableValues = {"RECEIVED", "PENDING", "VALIDATED", "PAID", "FAILED"}))
        })
    public ResponseEntity<Void> updateTransactionStatusPath(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status) {
        Optional<UUID> updated = transactionService.updateTransactionStatus(id.toString(), status);
        return updated.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/pending")
    @Operation(summary = "Set a transaction to PENDING", description = "Sets a 'VALIDATED' transaction to 'PENDING'",
        parameters = {
            @Parameter(name = "id", description = "ID of the transaction to be updated", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> setTransactionToPending(@RequestParam("id") String id) {
        transactionService.setTransactionToPending(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pending-merchant")
    @Operation(summary = "Set all transactions for a merchant to PENDING", description = "Sets all 'VALIDATED' transactions for a merchant to 'PENDING'",
        parameters = {
            @Parameter(name = "merchantId", description = "ID of the merchant", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction statuses updated successfully")
    })
    public ResponseEntity<Void> setAllTransactionsToPendingForMerchant(@RequestParam("merchantId") String merchantId) {
        transactionService.setAllTransactionsToPendingForMerchant(merchantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pending-all")
    @Operation(summary = "Set all validated transactions to PENDING", description = "Sets all 'VALIDATED' transactions to 'PENDING'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction statuses updated successfully")
    })
    public ResponseEntity<Void> setAllTransactionsToPending() {
        transactionService.setAllTransactionsToPending();
        return ResponseEntity.ok().build();
    }
}
