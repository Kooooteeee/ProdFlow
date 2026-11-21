package ru.prodflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartRequestRequestDto {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    private String description;
}
