package ru.prodflow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdateOrderDto;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.Order;
import ru.prodflow.model.OrderOperation;
import ru.prodflow.model.Part;
import ru.prodflow.model.PartOperation;
import ru.prodflow.repository.OrderOperationRepository;
import ru.prodflow.repository.OrderRepository;
import ru.prodflow.repository.PartOperationRepository;
import ru.prodflow.repository.UserRepository;
import ru.prodflow.service.interfaces.OrderService;
import ru.prodflow.service.interfaces.PartService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PartService partService;
    private final PartOperationRepository partOperationRepository;
    private final OrderOperationRepository orderOperationRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void createOrder(OrderRequestDto orderRequestDto) {
        Part part = partService.getPartById(orderRequestDto.getPartId());

        List<PartOperation> partOperations = getCheckedPartOperations(part.getId());

        Order order = new Order();
        order.setCustomer(userRepository.findById(1L).orElseThrow(() -> new NotFoundException("Пользователя нет.")));
        order.setPart(part);
        order.setQuantity(orderRequestDto.getQuantity());
        order.setStatus(OrderStatus.NEW);

        Order savedOrder = orderRepository.save(order);

        createOrderOperations(savedOrder, savedOrder.getCreatedAt(), partOperations);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Заказ не найден."));
    }

    @Transactional
    @Override
    public void updateOrder(Long id, UpdateOrderDto updateOrderDto) {
        Order order = getOrderById(id);

        if (order.getStatus() != OrderStatus.NEW) {
            throw new ConflictException("Этот заказ нельзя обновить.");
        }

        if (updateOrderDto.getQuantity() != null) order.setQuantity(updateOrderDto.getQuantity());

        if (updateOrderDto.getPartId() != null) {
            Part part = partService.getPartById(updateOrderDto.getPartId());

            List<PartOperation> partOperations = getCheckedPartOperations(part.getId());

            order.setPart(part);

            orderOperationRepository.deleteAllByOrder_Id(id);
            orderOperationRepository.flush();

            createOrderOperations(order, OffsetDateTime.now(), partOperations);

        }
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }

    @Transactional
    @Override
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != OrderStatus.NEW) {
            throw new ConflictException("Этот заказ нельзя отменить.");
        }

        orderOperationRepository.deleteAllByOrder_Id(id);
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public List<Order> findByCustomerId(Long id) {
        return orderRepository.findAllByCustomer_Id(id);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    private List<PartOperation> getCheckedPartOperations(Long id) {
        List<Long> availablePartIds = partService.findAvailableParts().stream()
                .map(Part::getId)
                .toList();

        if (!availablePartIds.contains(id)) {
            throw new ConflictException("Деталь с id: " + id + " сейчас недоступна.");
        }

        return partOperationRepository.
                findAllByPart_IdOrderById_SequenceAsc(id);
    }

    private void createOrderOperations(Order order, OffsetDateTime readyAt, List<PartOperation> partOperations) {
        boolean isFirst = true;

        for (PartOperation p : partOperations) {
            OrderOperation oo = new OrderOperation();
            oo.setOrder(order);
            oo.setOperation(p.getOperation());
            oo.setSequence(p.getId().getSequence());
            oo.setTimePerPart(p.getOperation().getDurationMinutes());
            oo.setStatus(OrderOperationStatus.PENDING);
            if (isFirst) {
                oo.setReadyAt(readyAt);
                isFirst = false;
            }
            orderOperationRepository.save(oo);
        }
    }
}
