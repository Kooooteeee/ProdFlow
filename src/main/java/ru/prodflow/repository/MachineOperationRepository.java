package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.model.MachineOperation;
import ru.prodflow.model.MachineOperationId;

public interface MachineOperationRepository extends JpaRepository<MachineOperation, MachineOperationId> {
    boolean existsByMachine_IsActiveTrueAndMachine_NeedsMaintenanceFalseAndOperation_IdAndMachine_StatusNot
            (Long id, MachineStatus status);
}
