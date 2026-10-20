package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.model.Machine;

import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByIsActiveTrue();

    List<Machine> findByStatus(MachineStatus status);

    List<Machine> findByStatusAndIsActiveTrueAndNeedsMaintenanceFalse(MachineStatus status);
}
