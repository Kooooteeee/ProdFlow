package ru.prodflow.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderDto {
    private Long partId;
    @Positive
    private Integer quantity;
}
