package ru.prodflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotEmpty
    private List<Long> operationIds;
}
