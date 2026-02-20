package com.dinoventures.wallet.transaction.controller;

import com.dinoventures.wallet.common.dto.ApiResponse;
import com.dinoventures.wallet.common.dto.PagedResponse;
import com.dinoventures.wallet.transaction.dto.*;
import com.dinoventures.wallet.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Wallet top-up, bonus, spend, credit/debit, and transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/credit")
    @Operation(summary = "Credit a wallet", description = "Adds the specified amount to the wallet balance.")
    public ResponseEntity<ApiResponse<TransactionResponse>> credit(
            @Valid @RequestBody CreditRequest request,
            @RequestHeader("X-Idempotency-Key") @Size(max = 255) @Parameter(description = "Client-generated UUID for idempotency") String idempotencyKey) {
        TransactionResponse response = transactionService.credit(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Credit processed", response));
    }

    @PostMapping("/debit")
    @Operation(summary = "Debit a wallet", description = "Subtracts the specified amount from the wallet balance.")
    public ResponseEntity<ApiResponse<TransactionResponse>> debit(
            @Valid @RequestBody DebitRequest request,
            @RequestHeader("X-Idempotency-Key") @Size(max = 255) String idempotencyKey) {
        TransactionResponse response = transactionService.debit(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Debit processed", response));
    }

    @PostMapping("/topup")
    @Operation(summary = "Top-up wallet (purchase credits)", description = "User purchases credits with real money.")
    public ResponseEntity<ApiResponse<TransactionResponse>> topUp(
            @Valid @RequestBody TopUpRequest request,
            @RequestHeader("X-Idempotency-Key") @Size(max = 255) String idempotencyKey) {
        TransactionResponse response = transactionService.topUp(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Top-up processed", response));
    }

    @PostMapping("/bonus")
    @Operation(summary = "Issue bonus/incentive", description = "System issues free credits to a user.")
    public ResponseEntity<ApiResponse<TransactionResponse>> bonus(
            @Valid @RequestBody BonusRequest request,
            @RequestHeader("X-Idempotency-Key") @Size(max = 255) String idempotencyKey) {
        TransactionResponse response = transactionService.bonus(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Bonus issued", response));
    }

    @PostMapping("/spend")
    @Operation(summary = "Spend credits (in-app purchase)", description = "User spends credits on an in-app item.")
    public ResponseEntity<ApiResponse<TransactionResponse>> spend(
            @Valid @RequestBody SpendRequest request,
            @RequestHeader("X-Idempotency-Key") @Size(max = 255) String idempotencyKey) {
        TransactionResponse response = transactionService.spend(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Spend processed", response));
    }

    @GetMapping("/wallet/{walletId}")
    @Operation(summary = "Get transaction history", description = "Paginated transaction history for a wallet.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getTransactionHistory(
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<TransactionResponse> history = transactionService.getTransactionHistory(walletId, page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/reference/{referenceId}")
    @Operation(summary = "Get transaction by reference ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReferenceId(@PathVariable String referenceId) {
        TransactionResponse response = transactionService.getTransactionByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/wallet/{walletId}/reconcile")
    @Operation(summary = "Reconcile wallet balance", description = "Compares stored balance with ledger-calculated balance.")
    public ResponseEntity<ApiResponse<BigDecimal>> reconcile(@PathVariable UUID walletId) {
        BigDecimal drift = transactionService.reconcile(walletId);
        String message = drift.compareTo(BigDecimal.ZERO) == 0 ? "Balance is consistent" : "DRIFT DETECTED: " + drift;
        return ResponseEntity.ok(ApiResponse.success(message, drift));
    }
}
