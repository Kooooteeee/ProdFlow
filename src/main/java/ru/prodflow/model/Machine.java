package ru.prodflow.model;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.prodflow.enums.MachineStatus;

@Entity
@Table(name = "machines")
@NoArgsConstructor
@Getter
@Setter
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MachineStatus status;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "maintenance_requested", nullable = false)
    private boolean needsMaintenance = false;
}
