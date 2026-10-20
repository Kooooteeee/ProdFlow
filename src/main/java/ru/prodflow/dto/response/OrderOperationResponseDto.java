package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.OrderOperationStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
public class OrderOperationResponseDto {
    private Long id;
    private Long orderId;
    private Long operationId;
    private String operationName;
    private Integer sequence;
    private OrderOperationStatus status;
    private OffsetDateTime plannedEndAt;
}
