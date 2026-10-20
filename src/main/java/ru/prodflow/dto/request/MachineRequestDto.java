package ru.prodflow.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MachineRequestDto {
    private String name;
    private Long operationId;
}