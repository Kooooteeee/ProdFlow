package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.PartRequestStatus;
import ru.prodflow.model.PartRequest;

import java.util.List;

public interface PartRequestRepository extends JpaRepository<PartRequest, Long> {
    List<PartRequest> findByStatus(PartRequestStatus status);

    List<PartRequest> findByCustomerId(Long customerId);
}