package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.UserRole;
import ru.prodflow.enums.UserStatus;

import java.time.OffsetDateTime;
@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private OffsetDateTime createdAt;
}
