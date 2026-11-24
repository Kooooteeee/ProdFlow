package ru.prodflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.prodflow.enums.PartRequestStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
public class AdminPartRequestResponseDto {
    private Long id;
    private String name;
    private String description;
    private PartRequestStatus status;
    private OffsetDateTime createdAt;
    private Long customerId;
    private String customerName;
    private String customerEmail;
}
