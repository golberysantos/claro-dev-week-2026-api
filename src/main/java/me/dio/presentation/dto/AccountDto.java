package me.dio.presentation.dto;

import java.math.BigDecimal;
import me.dio.domain.model.Account;

public record AccountDto(
        Long id,
        String number,
        String agency,
        BigDecimal balance,
        BigDecimal limit,
        BigDecimal pixDailyLimit) {

    public AccountDto(Account model) {
        this(
                model.getId(),
                model.getNumber(),
                model.getAgency(),
                model.getBalance(),
                model.getLimit(),
                model.getPixDailyLimit()
        );
    }

    public Account toModel() {
        return new Account(
                this.id,
                this.number,
                this.agency,
                this.balance,
                this.limit,
                this.pixDailyLimit
        );
    }
}
