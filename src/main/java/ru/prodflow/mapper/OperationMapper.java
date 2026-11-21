package ru.prodflow.mapper;

import ru.prodflow.dto.response.OperationResponseDto;
import ru.prodflow.model.Operation;

public class OperationMapper {

    public static OperationResponseDto toResponseDto(Operation operation) {
        OperationResponseDto dto = new OperationResponseDto();

        dto.setId(operation.getId());
        dto.setName(operation.getName());
        dto.setDurationMinutes(operation.getDurationMinutes());
        dto.setActive(operation.isActive());

        return dto;
    }
}