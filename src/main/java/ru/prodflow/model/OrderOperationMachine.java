package ru.prodflow.model;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_operation_machines")
@NoArgsConstructor
@Getter
@Setter
public class OrderOperationMachine {

    @EmbeddedId
    private OrderOperationMachineId id;

    @MapsId("orderOperationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_operation_id", nullable = false)
    private OrderOperation orderOperation;

    @MapsId("machineId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;
}
