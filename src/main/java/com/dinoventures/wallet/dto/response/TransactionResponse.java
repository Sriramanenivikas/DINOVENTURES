package com.dinoventures.wallet.dto.response;

import com.dinoventures.wallet.enums.TransactionStatus;
import com.dinoventures.wallet.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID walletId;
    private UUID counterWalletId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String description;
    private TransactionStatus status;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
