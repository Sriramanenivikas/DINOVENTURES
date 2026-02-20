package com.dinoventures.wallet.transaction.service;

import com.dinoventures.wallet.assettype.model.AssetType;
import com.dinoventures.wallet.common.dto.PagedResponse;
import com.dinoventures.wallet.idempotency.service.IdempotencyService;
import com.dinoventures.wallet.transaction.dto.*;
import com.dinoventures.wallet.transaction.model.Transaction;
import com.dinoventures.wallet.transaction.model.TransactionStatus;
import com.dinoventures.wallet.transaction.model.TransactionType;
import com.dinoventures.wallet.transaction.exception.InvalidAmountException;
import com.dinoventures.wallet.transaction.repository.TransactionRepository;
import com.dinoventures.wallet.wallet.model.Wallet;
import com.dinoventures.wallet.wallet.exception.InsufficientBalanceException;
import com.dinoventures.wallet.wallet.exception.WalletNotActiveException;
import com.dinoventures.wallet.wallet.exception.WalletNotFoundException;
import com.dinoventures.wallet.wallet.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Value("${wallet.transaction.max-page-size:100}")
    private int maxPageSize;

    @Value("${wallet.metadata.max-size-bytes:4096}")
    private int maxMetadataSizeBytes;

    public TransactionService(WalletRepository walletRepository, TransactionRepository transactionRepository,
                              IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransactionResponse credit(CreditRequest request, String idempotencyKey) {
        Optional<TransactionResponse> cached = idempotencyService
                .checkIdempotencyKey(idempotencyKey, TransactionType.CREDIT.name());
        if (cached.isPresent()) {
            log.info("Idempotency hit for key: {} — returning cached CREDIT response", idempotencyKey);
            return cached.get();
        }

        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + request.getWalletId()));

        validateWalletActive(wallet);
        validateAmountPrecision(request.getAmount(), wallet.getAssetType());
        validateMetadataSize(request.getMetadata());

        wallet.credit(request.getAmount());

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT)
                .amount(request.getAmount())
                .balanceAfter(wallet.getBalance())
                .referenceId(UUID.randomUUID().toString())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .metadata(request.getMetadata())
                .build();

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        TransactionResponse response = toResponse(transaction);
        idempotencyService.saveIdempotencyRecord(idempotencyKey, TransactionType.CREDIT.name(), 200, response);

        log.info("CREDIT {} {} to wallet {}", request.getAmount(), wallet.getAssetType().getSymbol(), wallet.getId());
        return response;
    }

    @Transactional
    public TransactionResponse debit(DebitRequest request, String idempotencyKey) {
        Optional<TransactionResponse> cached = idempotencyService
                .checkIdempotencyKey(idempotencyKey, TransactionType.DEBIT.name());
        if (cached.isPresent()) {
            log.info("Idempotency hit for key: {} — returning cached DEBIT response", idempotencyKey);
            return cached.get();
        }

        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + request.getWalletId()));

        validateWalletActive(wallet);
        validateAmountPrecision(request.getAmount(), wallet.getAssetType());
        validateMetadataSize(request.getMetadata());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance: available=%s, requested=%s (wallet: %s)",
                            wallet.getBalance(), request.getAmount(), wallet.getId()));
        }

        wallet.debit(request.getAmount());

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEBIT)
                .amount(request.getAmount())
                .balanceAfter(wallet.getBalance())
                .referenceId(UUID.randomUUID().toString())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .metadata(request.getMetadata())
                .build();

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        TransactionResponse response = toResponse(transaction);
        idempotencyService.saveIdempotencyRecord(idempotencyKey, TransactionType.DEBIT.name(), 200, response);

        log.info("DEBIT {} {} from wallet {}", request.getAmount(), wallet.getAssetType().getSymbol(), wallet.getId());
        return response;
    }

    @Transactional
    public TransactionResponse topUp(TopUpRequest request, String idempotencyKey) {
        Optional<TransactionResponse> cached = idempotencyService
                .checkIdempotencyKey(idempotencyKey, TransactionType.TOP_UP.name());
        if (cached.isPresent()) {
            return cached.get();
        }
        return executeDoubleEntry(request.getWalletId(), request.getAmount(), request.getDescription(),
                request.getMetadata(), TransactionType.TOP_UP, idempotencyKey, true);
    }

    @Transactional
    public TransactionResponse bonus(BonusRequest request, String idempotencyKey) {
        Optional<TransactionResponse> cached = idempotencyService
                .checkIdempotencyKey(idempotencyKey, TransactionType.BONUS.name());
        if (cached.isPresent()) {
            return cached.get();
        }
        return executeDoubleEntry(request.getWalletId(), request.getAmount(), request.getDescription(),
                request.getMetadata(), TransactionType.BONUS, idempotencyKey, true);
    }

    @Transactional
    public TransactionResponse spend(SpendRequest request, String idempotencyKey) {
        Optional<TransactionResponse> cached = idempotencyService
                .checkIdempotencyKey(idempotencyKey, TransactionType.SPEND.name());
        if (cached.isPresent()) {
            return cached.get();
        }
        return executeDoubleEntry(request.getWalletId(), request.getAmount(), request.getDescription(),
                request.getMetadata(), TransactionType.SPEND, idempotencyKey, false);
    }

    private TransactionResponse executeDoubleEntry(UUID userWalletId, BigDecimal amount, String description,
                                                   Map<String, Object> metadata, TransactionType type,
                                                   String idempotencyKey, boolean creditUser) {
        Wallet userWallet = walletRepository.findByIdForUpdate(userWalletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + userWalletId));

        validateWalletActive(userWallet);
        validateAmountPrecision(amount, userWallet.getAssetType());
        validateMetadataSize(metadata);

        Wallet treasuryWallet = findTreasuryWallet(userWallet.getAssetType().getId());

        // Lock in consistent order to prevent deadlocks
        if (userWallet.getId().compareTo(treasuryWallet.getId()) < 0) {
            walletRepository.findByIdForUpdate(userWallet.getId());
            walletRepository.findByIdForUpdate(treasuryWallet.getId());
        } else {
            walletRepository.findByIdForUpdate(treasuryWallet.getId());
            walletRepository.findByIdForUpdate(userWallet.getId());
        }

        String referenceId = UUID.randomUUID().toString();

        if (creditUser) {
            treasuryWallet.debit(amount);
            userWallet.credit(amount);
        } else {
            if (userWallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient balance: available=%s, requested=%s", userWallet.getBalance(), amount));
            }
            userWallet.debit(amount);
            treasuryWallet.credit(amount);
        }

        Transaction userTxn = Transaction.builder()
                .wallet(userWallet)
                .counterWalletId(treasuryWallet.getId())
                .type(type)
                .amount(amount)
                .balanceAfter(userWallet.getBalance())
                .referenceId(referenceId)
                .description(description)
                .status(TransactionStatus.SUCCESS)
                .metadata(metadata)
                .build();

        TransactionType treasuryType = creditUser ? TransactionType.DEBIT : TransactionType.CREDIT;
        Transaction treasuryTxn = Transaction.builder()
                .wallet(treasuryWallet)
                .counterWalletId(userWallet.getId())
                .type(treasuryType)
                .amount(amount)
                .balanceAfter(treasuryWallet.getBalance())
                .referenceId(referenceId + "-COUNTER")
                .description("[Treasury] " + (description != null ? description : type.name()))
                .status(TransactionStatus.SUCCESS)
                .metadata(metadata)
                .build();

        walletRepository.save(userWallet);
        walletRepository.save(treasuryWallet);
        transactionRepository.save(userTxn);
        transactionRepository.save(treasuryTxn);

        TransactionResponse response = toResponse(userTxn);
        idempotencyService.saveIdempotencyRecord(idempotencyKey, type.name(), 200, response);

        log.info("{} {} {} | user wallet {} | treasury wallet {}",
                type, amount, userWallet.getAssetType().getSymbol(), userWallet.getId(), treasuryWallet.getId());
        return response;
    }

    private Wallet findTreasuryWallet(UUID assetTypeId) {
        return walletRepository.findByUserIdAndAssetTypeId(SYSTEM_USER_ID, assetTypeId)
                .orElseThrow(() -> new WalletNotFoundException("Treasury wallet not found for asset type: " + assetTypeId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionHistory(UUID walletId, int page, int size) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }

        int cappedSize = Math.min(Math.max(size, 1), maxPageSize);
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize);
        Page<Transaction> txPage = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);

        return PagedResponse.<TransactionResponse>builder()
                .content(txPage.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .pageNumber(txPage.getNumber())
                .pageSize(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .first(txPage.isFirst())
                .last(txPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReferenceId(String referenceId) {
        Transaction transaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new WalletNotFoundException("Transaction not found with reference: " + referenceId));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public BigDecimal reconcile(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        BigDecimal ledgerBalance = transactionRepository.calculateLedgerBalance(walletId);
        BigDecimal drift = wallet.getBalance().subtract(ledgerBalance);

        if (drift.compareTo(BigDecimal.ZERO) != 0) {
            log.error("BALANCE DRIFT DETECTED for wallet {}: stored={}, ledger={}, drift={}",
                    walletId, wallet.getBalance(), ledgerBalance, drift);
        }
        return drift;
    }

    private void validateWalletActive(Wallet wallet) {
        if (!wallet.isTransactable()) {
            throw new WalletNotActiveException("Wallet " + wallet.getId() + " is " + wallet.getStatus());
        }
    }

    private void validateAmountPrecision(BigDecimal amount, AssetType assetType) {
        if (amount.stripTrailingZeros().scale() > assetType.getDecimalPlaces()) {
            throw new InvalidAmountException(String.format("Amount %s exceeds allowed precision for %s",
                    amount, assetType.getName()));
        }
    }

    private void validateMetadataSize(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(metadata);
            if (json.getBytes().length > maxMetadataSizeBytes) {
                throw new InvalidAmountException("Metadata exceeds maximum allowed size of " + maxMetadataSizeBytes + " bytes");
            }
        } catch (JsonProcessingException e) {
            throw new InvalidAmountException("Invalid metadata format");
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .counterWalletId(transaction.getCounterWalletId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .metadata(transaction.getMetadata())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
