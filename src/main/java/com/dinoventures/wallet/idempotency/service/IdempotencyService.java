package com.dinoventures.wallet.idempotency.service;

import com.dinoventures.wallet.idempotency.model.IdempotencyRecord;
import com.dinoventures.wallet.idempotency.exception.IdempotencyConflictException;
import com.dinoventures.wallet.idempotency.repository.IdempotencyRepository;
import com.dinoventures.wallet.transaction.dto.TransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Value("${wallet.idempotency.ttl-hours:24}")
    private int ttlHours;

    public IdempotencyService(IdempotencyRepository idempotencyRepository, ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Check if this idempotency key was already processed.
     * Returns the cached response if found and operation matches.
     * Throws IdempotencyConflictException if the key was used for a different
     * operation.
     */
    public Optional<TransactionResponse> checkIdempotencyKey(String key, String operation) {
        return idempotencyRepository.findByIdempotencyKey(key)
                .map(record -> {
                    if (!record.getOperation().equals(operation)) {
                        throw new IdempotencyConflictException(
                                "Idempotency key '" + key + "' was already used for a " + record.getOperation()
                                        + " operation");
                    }
                    try {
                        return objectMapper.readValue(record.getResponseBody(), TransactionResponse.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize cached idempotency response for key: {}", key, e);
                        return null;
                    }
                });
    }

    /**
     * Save the idempotency record within the SAME transaction as the wallet
     * operation.
     * This ensures the record commits/rolls back atomically with the wallet changes
     * —
     * preventing phantom cached responses for transactions that never completed.
     */
    @Transactional
    public void saveIdempotencyRecord(String key, String operation, int responseCode, TransactionResponse response) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .operation(operation)
                    .responseCode(responseCode)
                    .responseBody(responseBody)
                    .expiresAt(LocalDateTime.now().plusHours(ttlHours))
                    .build();
            idempotencyRepository.save(record);
            log.debug("Saved idempotency record for key: {} (operation: {})", key, operation);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response for idempotency key: {}", key, e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredKeys() {
        int deleted = idempotencyRepository.deleteExpiredRecords(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency records", deleted);
        }
    }
}
