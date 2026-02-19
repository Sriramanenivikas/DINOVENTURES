package com.dinoventures.wallet.repository;

import com.dinoventures.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        Page<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

        Optional<Transaction> findByReferenceId(String referenceId);

        @Query("SELECT t FROM Transaction t " +
                        "WHERE t.wallet.id = :walletId " +
                        "AND t.createdAt >= :from " +
                        "AND t.createdAt <= :to " +
                        "ORDER BY t.createdAt DESC")
        Page<Transaction> findByWalletIdAndDateRange(
                        @Param("walletId") UUID walletId,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to,
                        Pageable pageable);

        @Query("SELECT COALESCE(SUM(CASE WHEN t.type IN ('CREDIT','TOP_UP','BONUS') THEN t.amount ELSE -t.amount END), 0) "
                        +
                        "FROM Transaction t WHERE t.wallet.id = :walletId AND t.status = 'SUCCESS'")
        BigDecimal calculateLedgerBalance(@Param("walletId") UUID walletId);
}
