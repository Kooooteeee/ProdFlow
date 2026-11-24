package ru.prodflow.mapper;

import ru.prodflow.dto.response.MachineResponseDto;
import ru.prodflow.model.Machine;

public class MachineMapper {

    public static MachineResponseDto toResponseDto(Machine machine) {
        MachineResponseDto dto = new MachineResponseDto();

        dto.setId(machine.getId());
        dto.setName(machine.getName());
        dto.setStatus(machine.getStatus());
        dto.setActive(machine.isActive());
        dto.setNeedsMaintenance(machine.isNeedsMaintenance());

        return dto;
    }
}