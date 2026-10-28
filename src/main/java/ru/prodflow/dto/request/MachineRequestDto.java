package ru.prodflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MachineRequestDto {
    @NotNull
    private String name;
    @NotNull
    private Long operationId;
}