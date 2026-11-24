package ru.prodflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.UpdatePartDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.dto.response.PartResponseDto;
import ru.prodflow.mapper.PartMapper;
import ru.prodflow.model.Part;
import ru.prodflow.service.interfaces.PartService;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @GetMapping("/{id}")
    public PartResponseDto getPart(@PathVariable Long id) {
        Part part = partService.getPartById(id);
        return PartMapper.toResponseDto(part);
    }

    @GetMapping("/active")
    public List<PartResponseDto> getActiveParts() {
        List<Part> parts = partService.findActive();
        return parts.stream().map(PartMapper::toResponseDto).toList();
    }

    @GetMapping("/available")
    public List<PartResponseDto> getAvailableParts() {
        List<Part> parts = partService.findAvailableParts();
        return parts.stream().map(PartMapper::toResponseDto).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponseDto createPart(@Valid @RequestBody PartRequestDto partRequestDto) {
        Part part = partService.createPart(partRequestDto);
        return PartMapper.toResponseDto(part);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}")
    public void updatePart(@PathVariable Long id, @Valid @RequestBody UpdatePartDto updatePartDto) {
        partService.updatePart(id, updatePartDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}/archive")
    public void archivePart(@PathVariable Long id) {
        partService.archivePart(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}/restore")
    public void restorePart(@PathVariable Long id) {
        partService.restorePart(id);
    }
}
