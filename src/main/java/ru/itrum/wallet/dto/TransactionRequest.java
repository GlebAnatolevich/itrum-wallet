package ru.itrum.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.itrum.wallet.annotation.ValidOperationType;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransactionRequest {
    @NotNull
    private UUID walletId;

    @ValidOperationType
    private String operationType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
