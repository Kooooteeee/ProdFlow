package ru.prodflow.mapper;

import ru.prodflow.dto.response.OrderResponseDto;
import ru.prodflow.model.Order;

public class OrderMapper {

    public static OrderResponseDto toResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();

        dto.setId(order.getId());
        dto.setPartId(order.getPart().getId());
        dto.setPartName(order.getPart().getName());
        dto.setQuantity(order.getQuantity());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }
}