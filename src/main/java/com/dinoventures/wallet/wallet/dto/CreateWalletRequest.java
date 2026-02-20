package com.dinoventures.wallet.wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "assetTypeId is required")
    private UUID assetTypeId;
}
