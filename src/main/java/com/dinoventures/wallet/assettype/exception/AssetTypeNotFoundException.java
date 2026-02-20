package com.dinoventures.wallet.assettype.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AssetTypeNotFoundException extends RuntimeException {
    public AssetTypeNotFoundException(String message) {
        super(message);
    }
}
