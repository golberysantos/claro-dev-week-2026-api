package me.dio.presentation.dto;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.List;
import me.dio.domain.model.User;

public record UserDto(
        Long id,
        String name,
        AccountDto account,
        CardDto card,
        List<FeatureDto> features,
        List<NewsDto> news) {

    public UserDto(User model) {
        this(
                model.getId(),
                model.getName(),
                ofNullable(model.getAccount()).map(AccountDto::new).orElse(null),
                ofNullable(model.getCard()).map(CardDto::new).orElse(null),
                ofNullable(model.getFeatures()).orElse(emptyList()).stream().map(FeatureDto::new).collect(toList()),
                ofNullable(model.getNews()).orElse(emptyList()).stream().map(NewsDto::new).collect(toList())
        );
    }

    public User toModel() {
        return new User(
                this.id,
                this.name,
                ofNullable(this.account).map(AccountDto::toModel).orElse(null),
                ofNullable(this.card).map(CardDto::toModel).orElse(null),
                ofNullable(this.features).orElse(emptyList()).stream().map(FeatureDto::toModel).collect(toList()),
                ofNullable(this.news).orElse(emptyList()).stream().map(NewsDto::toModel).collect(toList())
        );
    }
}
