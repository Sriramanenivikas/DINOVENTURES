package com.dinoventures.wallet.wallet.service;

import com.dinoventures.wallet.assettype.model.AssetType;
import com.dinoventures.wallet.assettype.exception.AssetTypeNotFoundException;
import com.dinoventures.wallet.assettype.repository.AssetTypeRepository;
import com.dinoventures.wallet.wallet.dto.CreateWalletRequest;
import com.dinoventures.wallet.wallet.dto.WalletResponse;
import com.dinoventures.wallet.wallet.model.Wallet;
import com.dinoventures.wallet.wallet.model.WalletStatus;
import com.dinoventures.wallet.wallet.exception.DuplicateWalletException;
import com.dinoventures.wallet.wallet.exception.WalletNotFoundException;
import com.dinoventures.wallet.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final AssetTypeRepository assetTypeRepository;

    public WalletService(WalletRepository walletRepository, AssetTypeRepository assetTypeRepository) {
        this.walletRepository = walletRepository;
        this.assetTypeRepository = assetTypeRepository;
    }

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        AssetType assetType = assetTypeRepository.findById(request.getAssetTypeId())
                .orElseThrow(() -> new AssetTypeNotFoundException(
                        "Asset type not found with id: " + request.getAssetTypeId()));

        if (!assetType.isActive()) {
            throw new AssetTypeNotFoundException(
                    "Asset type '" + assetType.getName() + "' is inactive and cannot be used");
        }

        if (walletRepository.existsByUserIdAndAssetTypeId(request.getUserId(), request.getAssetTypeId())) {
            throw new DuplicateWalletException(
                    "User " + request.getUserId() + " already has a wallet for "
                            + assetType.getName());
        }

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .assetType(assetType)
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet saved = walletRepository.save(wallet);
        log.info("Created wallet {} for user {} (asset: {})",
                saved.getId(), saved.getUserId(), assetType.getName());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        Wallet wallet = walletRepository.findByIdWithAssetType(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with id: " + walletId));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsByUserId(UUID userId) {
        return walletRepository.findByUserIdWithAssetType(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with id: " + walletId));
        return wallet.getBalance();
    }

    private WalletResponse toResponse(Wallet wallet) {
        AssetType assetType = wallet.getAssetType();
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .assetTypeId(assetType.getId())
                .assetTypeName(assetType.getName())
                .assetTypeSymbol(assetType.getSymbol())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
