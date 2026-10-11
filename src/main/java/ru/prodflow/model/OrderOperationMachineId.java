package ru.prodflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderOperationMachineId implements Serializable {

    @Column(name = "order_operation_id", nullable = false)
    private Long orderOperationId;

    @Column(name = "machine_id", nullable = false)
    private Long machineId;
}
