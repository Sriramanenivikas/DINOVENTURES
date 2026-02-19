package com.dinoventures.wallet.dto.response;

import com.dinoventures.wallet.enums.WalletStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private UUID id;
    private UUID userId;
    private UUID assetTypeId;
    private String assetTypeName;
    private String assetTypeSymbol;
    private BigDecimal balance;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
