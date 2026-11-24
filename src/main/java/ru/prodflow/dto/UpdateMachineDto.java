package ru.prodflow.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMachineDto {
    @Pattern(regexp = "(?s).*\\S.*")
    @Size(max = 255)
    private String name;
    @Positive
    private Long operationId;
}
