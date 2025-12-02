package ru.itrum.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.itrum.wallet.dto.TransactionRequest;
import ru.itrum.wallet.dto.WalletResponse;
import ru.itrum.wallet.model.Wallet;
import ru.itrum.wallet.service.WalletService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<Void> processTransaction(@Valid @RequestBody TransactionRequest request) {
        walletService.processTransaction(request.getWalletId(), request.getOperationType(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID walletId) {
        Wallet wallet = walletService.getWallet(walletId);
        return ResponseEntity.ok(new WalletResponse(walletId, wallet.getBalance()));
    }
}
