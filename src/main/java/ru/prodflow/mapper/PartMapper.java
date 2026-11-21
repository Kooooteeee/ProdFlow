package ru.prodflow.mapper;

import ru.prodflow.dto.response.PartResponseDto;
import ru.prodflow.model.Part;

public class PartMapper {

    public static PartResponseDto toResponseDto(Part part) {
        PartResponseDto dto = new PartResponseDto();

        dto.setId(part.getId());
        dto.setName(part.getName());
        dto.setDescription(part.getDescription());
        dto.setActive(part.isActive());

        return dto;
    }
}