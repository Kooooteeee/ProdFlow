package ru.prodflow.mapper;

import ru.prodflow.dto.response.OrderOperationResponseDto;
import ru.prodflow.model.OrderOperation;

public class OrderOperationMapper {

    public static OrderOperationResponseDto toResponseDto(OrderOperation oo) {
        OrderOperationResponseDto dto = new OrderOperationResponseDto();

        dto.setId(oo.getId());
        dto.setOrderId(oo.getOrder().getId());
        dto.setOperationId(oo.getOperation().getId());
        dto.setOperationName(oo.getOperation().getName());
        dto.setSequence(oo.getSequence());
        dto.setStatus(oo.getStatus());
        dto.setPlannedEndAt(oo.getPlannedEndAt());

        return dto;
    }
}