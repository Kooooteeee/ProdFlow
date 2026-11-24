package ru.prodflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.UpdateMachineDto;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.dto.response.MachineResponseDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.mapper.MachineMapper;
import ru.prodflow.model.Machine;
import ru.prodflow.service.interfaces.MachineService;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping("/{id}")
    public MachineResponseDto getMachine(@PathVariable Long id) {
        Machine machine = machineService.getMachineById(id);
        return MachineMapper.toResponseDto(machine);
    }

    @GetMapping("/operation/{id}")
    public List<MachineResponseDto> getMachinesByOperationId(@PathVariable Long id) {
        return machineService.getMachinesByOperationId(id).stream()
                .map(MachineMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/status")
    public List<MachineResponseDto> getMachinesByStatus(@RequestParam MachineStatus status) {
        return machineService.getMachinesByStatus(status).stream()
                .map(MachineMapper::toResponseDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponseDto createMachine(
            @Valid @RequestBody MachineRequestDto machineRequestDto) {

        Machine machine = machineService.createMachine(machineRequestDto);
        return MachineMapper.toResponseDto(machine);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMachine(@PathVariable Long id,
                              @Valid @RequestBody UpdateMachineDto updateMachineDto) {
        machineService.updateMachine(id, updateMachineDto);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveMachine(@PathVariable Long id) {
        machineService.archiveMachine(id);
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMachine(@PathVariable Long id) {
        machineService.restoreMachine(id);
    }

    @PatchMapping("/maintenance/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendMachinesToMaintenance(@RequestBody List<Long> ids) {
        machineService.sendMachineToMaintenance(ids);
    }

    @PatchMapping("/maintenance/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeMaintenance(@RequestBody List<Long> ids) {
        machineService.completeMaintenance(ids);
    }
}