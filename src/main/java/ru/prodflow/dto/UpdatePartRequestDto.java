package ru.prodflow.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePartRequestDto {
    @Pattern(regexp = "(?s).*\\S.*")
    @Size(max = 255)
    private String name;
    @Pattern(regexp = "(?s).*\\S.*")
    private String description;
}
