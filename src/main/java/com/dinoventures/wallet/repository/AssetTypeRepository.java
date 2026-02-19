package com.dinoventures.wallet.repository;

import com.dinoventures.wallet.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, UUID> {

    Optional<AssetType> findByName(String name);

    List<AssetType> findByIsActiveTrue();

    boolean existsByName(String name);
}
