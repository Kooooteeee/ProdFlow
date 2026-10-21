package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.model.MachineOperation;
import ru.prodflow.model.MachineOperationId;

public interface MachineOperationRepository extends JpaRepository<MachineOperation, MachineOperationId> {
}
