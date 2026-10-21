package ru.prodflow.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartRequestDto {
    private String name;
    private String description;
    @NotEmpty
    private List<Long> operationIds;
}
