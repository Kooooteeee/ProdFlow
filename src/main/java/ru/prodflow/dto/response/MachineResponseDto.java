package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.MachineStatus;

@Getter
@Setter
public class MachineResponseDto {
    private Long id;

    private String name;

    private MachineStatus status;

    private boolean isActive;

    private boolean needsMaintenance;
}
