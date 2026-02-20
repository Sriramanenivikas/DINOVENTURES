package com.dinoventures.wallet.assettype.service;

import com.dinoventures.wallet.assettype.dto.CreateAssetTypeRequest;
import com.dinoventures.wallet.assettype.model.AssetType;
import com.dinoventures.wallet.assettype.exception.AssetTypeNotFoundException;
import com.dinoventures.wallet.assettype.repository.AssetTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssetTypeService {

    private static final Logger log = LoggerFactory.getLogger(AssetTypeService.class);

    private final AssetTypeRepository assetTypeRepository;

    public AssetTypeService(AssetTypeRepository assetTypeRepository) {
        this.assetTypeRepository = assetTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<AssetType> getActiveAssetTypes() {
        return assetTypeRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public AssetType getAssetTypeById(UUID id) {
        return assetTypeRepository.findById(id)
                .orElseThrow(() -> new AssetTypeNotFoundException(
                        "Asset type not found with id: " + id));
    }

    @Transactional
    public AssetType createAssetType(CreateAssetTypeRequest request) {
        if (assetTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Asset type with name '" + request.getName() + "' already exists");
        }

        AssetType assetType = AssetType.builder()
                .name(request.getName())
                .symbol(request.getSymbol())
                .description(request.getDescription())
                .decimalPlaces(request.getDecimalPlaces())
                .build();

        AssetType saved = assetTypeRepository.save(assetType);
        log.info("Created asset type: {} ({})", saved.getName(), saved.getId());
        return saved;
    }
}
