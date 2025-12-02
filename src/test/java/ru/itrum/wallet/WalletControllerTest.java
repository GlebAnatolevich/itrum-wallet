package ru.itrum.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.itrum.wallet.controller.WalletController;
import ru.itrum.wallet.dto.TransactionRequest;
import ru.itrum.wallet.exception.InsufficientFundsException;
import ru.itrum.wallet.exception.WalletNotFoundException;
import ru.itrum.wallet.model.Wallet;
import ru.itrum.wallet.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    private final UUID walletId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void validDepositReturns200() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setWalletId(walletId);
        req.setOperationType("DEPOSIT");
        req.setAmount(new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(walletService).processTransaction(walletId, "DEPOSIT", new BigDecimal("100"));
    }

    @Test
    void validGetBalanceReturns200() throws Exception {
        when(walletService.getWallet(walletId))
                .thenReturn(new Wallet() {{
                    setId(walletId);
                    setBalance(new BigDecimal("250.50"));
                }});

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(250.50));
    }

    @Test
    void withdrawWithInsufficientFundsReturns400() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setWalletId(walletId);
        req.setOperationType("WITHDRAW");
        req.setAmount(new BigDecimal("1000"));

        doThrow(new InsufficientFundsException())
                .when(walletService)
                .processTransaction(walletId, "WITHDRAW", new BigDecimal("1000"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств"));
    }

    @Test
    void nonExistentWalletReturns404() throws Exception {
        UUID badId = UUID.randomUUID();
        when(walletService.getWallet(badId))
                .thenThrow(new WalletNotFoundException());

        mockMvc.perform(get("/api/v1/wallets/" + badId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Кошелёк не найден"));
    }

    @Test
    void invalidOperationTypeReturns400() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setWalletId(walletId);
        req.setOperationType("INVALID");
        req.setAmount(new BigDecimal("50"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недопустимый формат запроса"));
    }

    @Test
    void invalidJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недопустимый формат запроса"));
    }
}
