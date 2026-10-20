package ru.prodflow.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartRequestDto {
    private String name;
    private String description;
    private List<Long> operationIds;
}
