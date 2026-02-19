package com.dinoventures.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Request to top-up a user's wallet with purchased credits.
 * Funds are transferred from the System Treasury to the user's wallet.
 */
@Schema(description = "Top-up request — user purchases credits with real money")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {

    @NotNull(message = "walletId is required")
    @Schema(description = "Target user wallet ID to credit", example = "aa100000-0000-0000-0000-000000000001")
    private UUID walletId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Schema(description = "Amount of credits to add", example = "100")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Transaction description", example = "Purchase of 100 Gold Coins — order #ORD-12345")
    private String description;

    @Schema(description = "Optional metadata (max 4KB)")
    private Map<String, Object> metadata;
}
