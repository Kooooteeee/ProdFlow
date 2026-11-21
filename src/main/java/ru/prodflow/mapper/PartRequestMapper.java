package ru.prodflow.mapper;

import ru.prodflow.dto.response.AdminPartRequestResponseDto;
import ru.prodflow.dto.response.PartRequestResponseDto;
import ru.prodflow.model.PartRequest;

public class PartRequestMapper {

    public static PartRequestResponseDto toResponseDto(PartRequest partRequest) {
        PartRequestResponseDto dto = new PartRequestResponseDto();

        dto.setId(partRequest.getId());
        dto.setName(partRequest.getName());
        dto.setStatus(partRequest.getStatus());
        dto.setCreatedAt(partRequest.getCreatedAt());

        return dto;
    }

    public static AdminPartRequestResponseDto toAdminResponseDto(PartRequest partRequest) {
        AdminPartRequestResponseDto dto = new AdminPartRequestResponseDto();

        dto.setId(partRequest.getId());
        dto.setName(partRequest.getName());
        dto.setDescription(partRequest.getDescription());
        dto.setStatus(partRequest.getStatus());
        dto.setCreatedAt(partRequest.getCreatedAt());
        dto.setCustomerId(partRequest.getCustomer().getId());
        dto.setCustomerName(partRequest.getCustomer().getName());
        dto.setCustomerEmail(partRequest.getCustomer().getEmail());

        return dto;
    }
}