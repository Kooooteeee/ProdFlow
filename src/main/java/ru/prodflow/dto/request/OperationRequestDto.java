package ru.prodflow.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationRequestDto {
    private String name;
    private Integer durationMinutes;
}
