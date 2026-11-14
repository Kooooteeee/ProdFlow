package ru.prodflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    @Pattern(regexp = "(?s).*\\S.*")
    private String name;
    @Email
    @Pattern(regexp = "(?s).*\\S.*")
    private String email;
}
