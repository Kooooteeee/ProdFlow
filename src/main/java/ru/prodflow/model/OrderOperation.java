package ru.prodflow.model;
import jakarta.persistence.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.prodflow.enums.OrderOperationStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_operations")
@NoArgsConstructor
@Getter
@Setter
public class OrderOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "time_per_part", nullable = false)
    private Integer timePerPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderOperationStatus status;

    @Column(name = "ready_at")
    private OffsetDateTime readyAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "planned_end_at")
    private OffsetDateTime plannedEndAt;
}
