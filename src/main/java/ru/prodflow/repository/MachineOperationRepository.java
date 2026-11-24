package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.model.MachineOperation;
import ru.prodflow.model.MachineOperationId;

import java.util.List;
import java.util.Optional;

public interface MachineOperationRepository extends JpaRepository<MachineOperation, MachineOperationId> {

    List<MachineOperation> findByOperation_Id(Long id);

    Optional<MachineOperation> findByMachine_Id(Long id);

    void deleteByMachine_Id(Long id);

    boolean existsByMachine_IsActiveTrueAndMachine_NeedsMaintenanceFalseAndOperation_IdAndMachine_StatusNot
            (Long id, MachineStatus status);
}
