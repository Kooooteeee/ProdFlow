package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdateMachineDto;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.model.Machine;

import java.util.List;

public interface MachineService {
    Machine createMachine(MachineRequestDto machineRequestDto);

    void updateMachine(Long id, UpdateMachineDto updateMachineDto);

    List<Machine>  getMachinesByOperationId(Long id);

    Machine getMachineById(Long id);

    List<Machine>  getMachinesByStatus(MachineStatus status);

    void archiveMachine(Long id);

    void restoreMachine(Long id);

    void makeMachinesBusy(List<Long> ids);

    void makeMachinesAvailable(List<Long> ids);

    void sendMachineToMaintenance(List<Long> ids);

    void completeMaintenance(List<Long> ids);
}
