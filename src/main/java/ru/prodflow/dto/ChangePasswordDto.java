package ru.prodflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    @NotBlank
    private String oldPassword;
    @Size(min = 8)
    @NotBlank
    private String newPassword;
}
