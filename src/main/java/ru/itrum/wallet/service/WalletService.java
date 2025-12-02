package ru.itrum.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itrum.wallet.exception.InsufficientFundsException;
import ru.itrum.wallet.exception.WalletNotFoundException;
import ru.itrum.wallet.model.Wallet;
import ru.itrum.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * Кошелёк должен существовать до транзакции. Условия ТЗ не подразумевают автоматическое создание кошелька при
     * первом депозите в случае его отсутствия.
     */
    @Transactional
    public void processTransaction(UUID walletId, String operationType, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(WalletNotFoundException::new);

        if ("DEPOSIT".equals(operationType)) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else if ("WITHDRAW".equals(operationType)) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException();
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else {
            throw new IllegalArgumentException("Недопустимый тип операции");
        }

        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(WalletNotFoundException::new);
    }
}
