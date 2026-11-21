package ru.prodflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartRequestDto {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    private String description;
    @NotEmpty
    @NotNull
    @Positive
    private List<Long> operationIds;
}
