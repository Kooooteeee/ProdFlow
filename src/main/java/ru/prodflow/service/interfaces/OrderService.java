package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdateOrderDto;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequestDto orderRequestDto);

    Order getOrderById(Long id);

    void updateOrder(Long id, UpdateOrderDto updateOrderDto);

    List<Order> findAllByStatus(OrderStatus status);

    void cancelOrder(Long id);

    List<Order> findByCustomerId(Long id);

    List<Order> findAll();
}
