package com.dinoventures.wallet.wallet.repository;

import com.dinoventures.wallet.wallet.model.Wallet;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT w FROM Wallet w JOIN FETCH w.assetType WHERE w.userId = :userId")
    List<Wallet> findByUserIdWithAssetType(@Param("userId") UUID userId);

    boolean existsByUserIdAndAssetTypeId(UUID userId, UUID assetTypeId);

    @EntityGraph(attributePaths = { "assetType" })
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithAssetType(@Param("id") UUID id);

    @EntityGraph(attributePaths = { "assetType" })
    Optional<Wallet> findByUserIdAndAssetTypeId(UUID userId, UUID assetTypeId);
}
