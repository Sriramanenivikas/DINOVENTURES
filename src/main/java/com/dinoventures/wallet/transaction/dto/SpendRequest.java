package com.dinoventures.wallet.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Spend request — user purchases an in-app item with credits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendRequest {

    @NotNull(message = "walletId is required")
    @Schema(description = "User wallet ID to debit", example = "aa100000-0000-0000-0000-000000000001")
    private UUID walletId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Schema(description = "Amount to spend", example = "30")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "What was purchased", example = "Dragon Armor — item #ITEM-456")
    private String description;

    @Schema(description = "Optional metadata (max 4KB)")
    private Map<String, Object> metadata;
}
