package me.dio.presentation.dto;

import java.math.BigDecimal;

public record TransferDto(
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount) {
}
