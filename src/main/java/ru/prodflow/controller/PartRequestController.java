package ru.prodflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.UpdatePartRequestDto;
import ru.prodflow.dto.request.PartRequestRequestDto;
import ru.prodflow.dto.response.AdminPartRequestResponseDto;
import ru.prodflow.dto.response.PartRequestResponseDto;
import ru.prodflow.enums.PartRequestStatus;
import ru.prodflow.mapper.PartRequestMapper;
import ru.prodflow.model.PartRequest;
import ru.prodflow.model.User;
import ru.prodflow.service.interfaces.PartRequestService;
import ru.prodflow.service.interfaces.UserService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/part-requests")
@RequiredArgsConstructor
public class PartRequestController {

    private final PartRequestService partRequestService;

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public PartRequestResponseDto createPartRequest(
            @Valid @RequestBody PartRequestRequestDto partRequestDto,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        PartRequest partRequest = partRequestService.createPartRequest(
                partRequestDto, user.getId());

        return PartRequestMapper.toResponseDto(partRequest);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<PartRequestResponseDto> getMyPartRequests(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        return partRequestService.findPartRequestByCustomerId(user.getId()).stream()
                .map(PartRequestMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PartRequestResponseDto getMyPartRequest(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        PartRequest partRequest = partRequestService.findPartRequestById(id);

        if (!Objects.equals(partRequest.getCustomer().getId(), user.getId())) {
            throw new AccessDeniedException("Вам недоступна эта заявка.");
        }

        return PartRequestMapper.toResponseDto(partRequest);
    }

    @PatchMapping("/my/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyPartRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartRequestDto updatePartRequestDto,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        PartRequest partRequest = partRequestService.findPartRequestById(id);

        if (!Objects.equals(partRequest.getCustomer().getId(), user.getId())) {
            throw new AccessDeniedException("Вам недоступна эта заявка.");
        }

        partRequestService.updatePartRequest(id, updatePartRequestDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public AdminPartRequestResponseDto getPartRequest(@PathVariable Long id) {
        PartRequest partRequest = partRequestService.findPartRequestById(id);
        return PartRequestMapper.toAdminResponseDto(partRequest);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<AdminPartRequestResponseDto> getPartRequestsByStatus(
            @RequestParam PartRequestStatus status) {

        return partRequestService.findPartRequestsByStatus(status).stream()
                .map(PartRequestMapper::toAdminResponseDto)
                .toList();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePartRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartRequestDto updatePartRequestDto) {

        partRequestService.updatePartRequest(id, updatePartRequestDto);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approvePartRequest(
            @PathVariable Long id,
            @RequestBody List<Long> operationIds) {

        partRequestService.approvePartRequest(id, operationIds);
    }

    @PatchMapping("/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectPartRequests(@RequestBody List<Long> ids) {
        partRequestService.rejectPartRequests(ids);
    }
}