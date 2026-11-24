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
public class MachineOperationId implements Serializable {

    @Column(name = "machine_id", nullable = false)
    private Long machineId;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;
}
