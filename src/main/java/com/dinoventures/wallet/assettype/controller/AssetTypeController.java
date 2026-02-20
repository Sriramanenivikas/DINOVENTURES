package com.dinoventures.wallet.assettype.controller;

import com.dinoventures.wallet.assettype.dto.CreateAssetTypeRequest;
import com.dinoventures.wallet.assettype.model.AssetType;
import com.dinoventures.wallet.assettype.service.AssetTypeService;
import com.dinoventures.wallet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-types")
@Tag(name = "Asset Types", description = "Manage virtual currency types (Gold Coins, Diamonds, etc.)")
public class AssetTypeController {

    private final AssetTypeService assetTypeService;

    public AssetTypeController(AssetTypeService assetTypeService) {
        this.assetTypeService = assetTypeService;
    }

    @GetMapping
    @Operation(summary = "List active asset types", description = "Returns all active virtual currency types")
    public ResponseEntity<ApiResponse<List<AssetType>>> getActiveAssetTypes() {
        List<AssetType> assetTypes = assetTypeService.getActiveAssetTypes();
        return ResponseEntity.ok(ApiResponse.success(assetTypes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset type by ID")
    public ResponseEntity<ApiResponse<AssetType>> getAssetTypeById(@PathVariable UUID id) {
        AssetType assetType = assetTypeService.getAssetTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(assetType));
    }

    @PostMapping
    @Operation(summary = "Create a new asset type", description = "Defines a new virtual currency")
    public ResponseEntity<ApiResponse<AssetType>> createAssetType(
            @Valid @RequestBody CreateAssetTypeRequest request) {
        AssetType created = assetTypeService.createAssetType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset type created", created));
    }
}
