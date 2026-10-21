package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.model.Operation;
import ru.prodflow.model.PartOperation;
import ru.prodflow.model.PartOperationId;

import java.util.List;

public interface PartOperationRepository extends JpaRepository<PartOperation, PartOperationId> {
    void deleteAllByPart_id(Long id);

    boolean existsByPart_IdAndOperation_IsActiveFalse(Long id);

    List<PartOperation> findAllByPart_IdOrderById_SequenceAsc(Long id);
}
