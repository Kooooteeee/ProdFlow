package ru.prodflow.dto;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePartDto {
    @Pattern(regexp = "(?s).*\\S.*")
    @Size(max = 255)
    private String name;
    @Pattern(regexp = "(?s).*\\S.*")
    private String description;
    @Size(min = 1)
    private List<@Positive Long> operationIds;
}

