package ru.itrum.wallet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.itrum.wallet.exception.InsufficientFundsException;
import ru.itrum.wallet.exception.WalletNotFoundException;
import ru.itrum.wallet.model.Wallet;
import ru.itrum.wallet.repository.WalletRepository;
import ru.itrum.wallet.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(WalletService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletServiceTest {

    @Autowired
    private WalletRepository walletRepository;

    private WalletService walletService;

    private final UUID walletId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository);
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.00"));
        walletRepository.save(wallet);
    }

    @Test
    void depositIncreasesBalance() {
        walletService.processTransaction(walletId, "DEPOSIT", new BigDecimal("50"));
        assertThat(walletRepository.findById(walletId).get().getBalance())
                .isEqualByComparingTo("150.00");
    }

    @Test
    void withdrawDecreasesBalance() {
        walletService.processTransaction(walletId, "WITHDRAW", new BigDecimal("30"));
        assertThat(walletRepository.findById(walletId).get().getBalance())
                .isEqualByComparingTo("70.00");
    }

    @Test
    void withdrawFailsOnInsufficientFunds() {
        assertThatThrownBy(() ->
                walletService.processTransaction(walletId, "WITHDRAW", new BigDecimal("200")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void nonExistentWalletThrows() {
        assertThatThrownBy(() ->
                walletService.processTransaction(UUID.randomUUID(), "DEPOSIT", BigDecimal.TEN))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void invalidOperationTypeThrows() {
        assertThatThrownBy(() ->
                walletService.processTransaction(walletId, "TRANSFER", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
