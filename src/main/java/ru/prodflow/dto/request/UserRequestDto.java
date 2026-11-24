package ru.prodflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
    @NotBlank
    @Size(min = 8)
    private String password;
}
