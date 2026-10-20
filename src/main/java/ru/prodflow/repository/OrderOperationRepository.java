package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.model.OrderOperation;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderOperationRepository extends JpaRepository<OrderOperation, Long> {
    List<OrderOperation> findByOrderIdOrderBySequenceAsc(Long orderId);

    List<OrderOperation> findByStatusAndPlannedEndAtLessThanEqual(OrderOperationStatus status, OffsetDateTime now);

    List<OrderOperation> findByStatusAndReadyAtLessThanEqualOrderByReadyAtAsc(OrderOperationStatus status,
                                                                              OffsetDateTime now);
}
