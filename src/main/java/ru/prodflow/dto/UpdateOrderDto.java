package ru.prodflow.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderDto {
    @Positive
    private Long partId;
    @Positive
    private Integer quantity;
}
