package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationResponseDto {
    private Long id;

    private String name;

    private Integer durationMinutes;

    private boolean isActive;
}
