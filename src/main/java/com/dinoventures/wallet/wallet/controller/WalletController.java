package com.dinoventures.wallet.wallet.controller;

import com.dinoventures.wallet.common.dto.ApiResponse;
import com.dinoventures.wallet.wallet.dto.CreateWalletRequest;
import com.dinoventures.wallet.wallet.dto.WalletResponse;
import com.dinoventures.wallet.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Create and manage user wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(summary = "Create a new wallet", description = "Creates a wallet for a user for a specific asset type. One wallet per user per asset type.")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        WalletResponse wallet = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created", wallet));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID", description = "Returns wallet details including asset type info")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID id) {
        WalletResponse wallet = walletService.getWallet(id);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all wallets for a user", description = "Returns all wallets (all asset types) for a specific user")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getWalletsByUserId(
            @PathVariable UUID userId) {
        List<WalletResponse> wallets = walletService.getWalletsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(wallets));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get wallet balance", description = "Quick balance check — returns just the balance amount")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable UUID id) {
        BigDecimal balance = walletService.getBalance(id);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}
