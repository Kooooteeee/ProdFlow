package ru.prodflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);

    List<Order> findAllByStatus(OrderStatus status);

    List<Order> findAllByCustomer_Id(Long id);
}
