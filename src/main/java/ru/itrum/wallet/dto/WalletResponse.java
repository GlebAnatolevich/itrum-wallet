package ru.itrum.wallet.dto;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class WalletResponse {
    private final UUID walletId;
    private final BigDecimal balance;

    public WalletResponse(UUID walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }
}
