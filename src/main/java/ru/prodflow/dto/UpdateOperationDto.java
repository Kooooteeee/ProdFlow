package ru.prodflow.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOperationDto {
    @Size(max = 255)
    private String name;
    @Positive
    private Integer durationMinutes;
}
