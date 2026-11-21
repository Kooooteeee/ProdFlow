package ru.prodflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    @Pattern(regexp = "(?s).*\\S.*")
    @Size(max = 255)
    private String name;
    @Email
    @Pattern(regexp = "(?s).*\\S.*")
    @Size(max = 255)
    private String email;
}
