package ru.prodflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequestDto {
    @NotNull
    private Long partId;
    @NotNull
    @Positive
    private Integer quantity;
}
