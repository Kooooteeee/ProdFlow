package ru.prodflow.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.ChangePasswordDto;
import ru.prodflow.dto.UpdateUserDto;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.dto.response.UserResponseDto;
import ru.prodflow.enums.UserRole;
import ru.prodflow.enums.UserStatus;
import ru.prodflow.mapper.UserMapper;
import ru.prodflow.model.User;
import ru.prodflow.service.interfaces.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return UserMapper.toResponseDto(userService.createUser(userRequestDto));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserByCustomer(@Valid @RequestBody UpdateUserDto updateUserDto, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        userService.updateUser(user.getId(), updateUserDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable Long id,
                           @Valid @RequestBody UpdateUserDto updateUserDto,
                           Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        if (user.getRole() == UserRole.ADMIN && userService.findUserById(id).getRole() == UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Недостаточно прав для изменения этого пользователя.");
        }
        userService.updateUser(id, updateUserDto);
    }

    @PatchMapping("/promote")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promoteUsersToAdmins(@RequestBody List<Long> ids) {
        userService.promoteUsersToAdmins(ids);
    }

    @PatchMapping("/deactivate/cus")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUsers(@RequestBody List<Long> ids) {
        userService.deactivateUsers(ids);
    }

    @PatchMapping("/reactivate/cus")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateUsers(@RequestBody List<Long> ids) {
        userService.reactivateUsers(ids);
    }

    @PatchMapping("/deactivate/adm")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateAdmins(@RequestBody List<Long> ids) {
        userService.deactivateAdmins(ids);
    }

    @PatchMapping("/reactivate/adm")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateAdmins(@RequestBody List<Long> ids) {
        userService.reactivateAdmins(ids);
    }

    @PatchMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveUsers(@RequestBody List<Long> ids) {
        userService.approveUsers(ids);
    }

    @PatchMapping("/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectUsers(@RequestBody List<Long> ids) {
        userService.rejectUsers(ids);
    }

    @PatchMapping("/update/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                               Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        userService.changePassword(user.getId(),
                changePasswordDto.getOldPassword(),
                changePasswordDto.getNewPassword());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserResponseDto findUserById(@PathVariable Long id) {
        return UserMapper.toResponseDto(userService.findUserById(id));
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserResponseDto findUserByEmail(@RequestParam String email) {
        return UserMapper.toResponseDto(userService.findUserByEmail(email));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserResponseDto> findUsersByStatus(@RequestParam UserStatus userStatus) {
        return userService.findUsersByStatus(userStatus).stream().map(UserMapper::toResponseDto).toList();
    }

    @GetMapping("/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserResponseDto> findUsersByRole(@RequestParam UserRole userRole) {
        return userService.findUsersByRole(userRole).stream().map(UserMapper::toResponseDto).toList();
    }
}
