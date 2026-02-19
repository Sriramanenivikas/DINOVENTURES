package com.dinoventures.wallet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {

    @NotNull(message = "walletId is required")
    private UUID walletId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    @DecimalMax(value = "999999999999.9999", message = "amount exceeds maximum allowed value")
    private BigDecimal amount;

    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    /**
     * Optional metadata (e.g., game level, item ID, source system).
     * Limited to 4KB serialized size — enforced in service layer.
     */
    private Map<String, Object> metadata;
}
