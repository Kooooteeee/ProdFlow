package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartResponseDto {
    private Long id;

    private String name;

    private String description;

    private boolean isActive;
}
