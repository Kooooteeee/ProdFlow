package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.PartRequestStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
public class PartRequestResponseDto {
    private Long id;
    private String name;
    private PartRequestStatus status;
    private OffsetDateTime createdAt;
}
