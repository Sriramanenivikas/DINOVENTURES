package com.dinoventures.wallet.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Bonus request — system issues free credits to a user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusRequest {

    @NotNull(message = "walletId is required")
    @Schema(description = "Target user wallet ID to credit", example = "aa100000-0000-0000-0000-000000000001")
    private UUID walletId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Schema(description = "Bonus amount to issue", example = "50")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Reason for the bonus", example = "Referral bonus — invited user #U-789")
    private String description;

    @Schema(description = "Optional metadata (max 4KB)")
    private Map<String, Object> metadata;
}
