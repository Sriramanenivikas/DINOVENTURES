package com.dinoventures.wallet.assettype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "Request to create a new virtual currency type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssetTypeRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    @Schema(description = "Unique name for the asset type", example = "Gold Coins")
    private String name;

    @NotBlank(message = "symbol is required")
    @Size(max = 20, message = "symbol must not exceed 20 characters")
    @Schema(description = "Short symbol/ticker", example = "GC")
    private String symbol;

    @Size(max = 500, message = "description must not exceed 500 characters")
    @Schema(description = "Description of the asset type", example = "Primary in-game currency earned through gameplay")
    private String description;

    @NotNull(message = "decimalPlaces is required")
    @Min(value = 0, message = "decimalPlaces must be >= 0")
    @Max(value = 8, message = "decimalPlaces must be <= 8")
    @Schema(description = "Number of decimal places allowed (0 for whole numbers)", example = "0")
    private Integer decimalPlaces;
}
