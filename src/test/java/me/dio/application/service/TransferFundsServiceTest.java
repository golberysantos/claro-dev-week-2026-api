package me.dio.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import me.dio.domain.exception.BusinessException;
import me.dio.domain.model.Account;
import me.dio.domain.model.User;
import me.dio.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransferFundsServiceTest {

    private UserRepository userRepository;
    private TransferFundsService transferFundsService;

    private User sourceUser;
    private User destinationUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        transferFundsService = new TransferFundsService(userRepository);

        Account sourceAccount = new Account(1L, "1111", "0001", new BigDecimal("1000.00"), new BigDecimal("200.00"), new BigDecimal("500.00"));
        sourceUser = new User(1L, "Source User", sourceAccount, null, null, null);

        Account destinationAccount = new Account(2L, "2222", "0001", new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("500.00"));
        destinationUser = new User(2L, "Destination User", destinationAccount, null, null, null);
    }

    @Test
    void shouldTransferFundsSuccessfully() {
        // Arrange
        String sourceNum = "1111";
        String destNum = "2222";
        BigDecimal amount = new BigDecimal("300.00");

        when(userRepository.findByAccountNumber(sourceNum)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findByAccountNumber(destNum)).thenReturn(Optional.of(destinationUser));
        when(userRepository.getDailyPixTotal(eq(1L), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        // Act
        transferFundsService.transfer(sourceNum, destNum, amount);

        // Assert
        assertEquals(new BigDecimal("700.00"), sourceUser.getAccount().getBalance());
        assertEquals(new BigDecimal("800.00"), destinationUser.getAccount().getBalance());
        verify(userRepository, times(1)).save(sourceUser);
        verify(userRepository, times(1)).save(destinationUser);
        verify(userRepository, times(2)).saveTransaction(any(Long.class), any());
    }

    @Test
    void shouldFailWhenBalanceIsInsufficient() {
        // Arrange
        String sourceNum = "1111";
        String destNum = "2222";
        BigDecimal amount = new BigDecimal("1500.00"); // Saldo disponível = 1000 + 200 = 1200

        // Aumenta o limite diário Pix para não bater nessa validação antes do saldo
        sourceUser.getAccount().setPixDailyLimit(new BigDecimal("2000.00"));

        when(userRepository.findByAccountNumber(sourceNum)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findByAccountNumber(destNum)).thenReturn(Optional.of(destinationUser));
        when(userRepository.getDailyPixTotal(eq(1L), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transferFundsService.transfer(sourceNum, destNum, amount);
        });

        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenDailyPixLimitExceeded() {
        // Arrange
        String sourceNum = "1111";
        String destNum = "2222";
        BigDecimal amount = new BigDecimal("300.00"); // Limite Pix = 500

        when(userRepository.findByAccountNumber(sourceNum)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findByAccountNumber(destNum)).thenReturn(Optional.of(destinationUser));
        // Já transferiu 300 hoje, logo 300 + 300 = 600 > 500
        when(userRepository.getDailyPixTotal(eq(1L), any(LocalDate.class))).thenReturn(new BigDecimal("300.00"));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transferFundsService.transfer(sourceNum, destNum, amount);
        });

        assertTrue(exception.getMessage().contains("limite Pix diário"));
        verify(userRepository, never()).save(any());
    }
}
