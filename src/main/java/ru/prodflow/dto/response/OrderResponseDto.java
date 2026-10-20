package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.OrderStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
public class OrderResponseDto {
    private Long id;
    private Long partId;
    private String partName;
    private Integer quantity;
    private OrderStatus status;
    private OffsetDateTime createdAt;
}