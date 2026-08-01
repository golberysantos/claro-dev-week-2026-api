package me.dio.presentation.dto;

import java.math.BigDecimal;
import me.dio.domain.model.Card;

public record CardDto(
        Long id,
        String number,
        BigDecimal limit,
        BigDecimal maxLimit,
        boolean active) {

    public CardDto(Card model) {
        this(
                model.getId(),
                model.getNumber(),
                model.getLimit(),
                model.getMaxLimit(),
                model.isActive()
        );
    }

    public Card toModel() {
        return new Card(
                this.id,
                this.number,
                this.limit,
                this.maxLimit,
                this.active
        );
    }
}
