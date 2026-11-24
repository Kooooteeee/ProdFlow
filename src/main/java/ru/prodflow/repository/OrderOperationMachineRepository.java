package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.model.OrderOperationMachine;
import ru.prodflow.model.OrderOperationMachineId;

import java.util.List;

public interface OrderOperationMachineRepository extends JpaRepository<OrderOperationMachine, OrderOperationMachineId> {

    List<OrderOperationMachine> findByOrderOperation_Id(Long id);

}
