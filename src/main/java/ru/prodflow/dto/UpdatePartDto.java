package ru.prodflow.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePartDto {
    private String name;
    private String description;
    private List<Long> operationIds;
}

