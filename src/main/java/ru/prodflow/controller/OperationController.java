package ru.prodflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.UpdateOperationDto;
import ru.prodflow.dto.request.OperationRequestDto;
import ru.prodflow.dto.response.OperationResponseDto;
import ru.prodflow.mapper.OperationMapper;
import ru.prodflow.model.Operation;
import ru.prodflow.service.interfaces.OperationService;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    @GetMapping("/{id}")
    public OperationResponseDto getOperation(@PathVariable Long id) {
        Operation operation = operationService.getOperationById(id);
        return OperationMapper.toResponseDto(operation);
    }

    @GetMapping
    public List<OperationResponseDto> getAllOperations() {
        return operationService.getAllOperations().stream()
                .map(OperationMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/active")
    public List<OperationResponseDto> getActiveOperations() {
        return operationService.getActiveOperations().stream()
                .map(OperationMapper::toResponseDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponseDto createOperation(
            @Valid @RequestBody OperationRequestDto operationRequestDto) {

        Operation operation = operationService.createOperation(operationRequestDto);
        return OperationMapper.toResponseDto(operation);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOperation(@PathVariable Long id,
                                @Valid @RequestBody UpdateOperationDto updateOperationDto) {
        operationService.updateOperation(id, updateOperationDto);
    }

    @PatchMapping("/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveOperations(@RequestBody List<Long> ids) {
        operationService.archiveOperations(ids);
    }

    @PatchMapping("/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreOperations(@RequestBody List<Long> ids) {
        operationService.restoreOperations(ids);
    }
}