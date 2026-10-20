package ru.prodflow.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequestDto {
    private Long partId;
    private Integer quantity;
}
