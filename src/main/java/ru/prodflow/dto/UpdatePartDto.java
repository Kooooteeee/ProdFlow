package ru.prodflow.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePartDto {
    private String name;
    private String description;
    @Size(min = 1)
    private List<Long> operationIds;
}

