package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.prodflow.dto.UpdateOrderDto;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.*;
import ru.prodflow.repository.*;
import ru.prodflow.service.interfaces.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private PartOperationRepository partOperationRepository;

    @Autowired
    private OrderOperationRepository orderOperationRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private MachineOperationRepository machineOperationRepository;

    @Test
    void getOrderById_WhenNotFound_ThrowsException() {
        assertThrows(NotFoundException.class, () -> orderService.getOrderById(-1L));
    }

    @Transactional
    @Test
    void getOrderById_WhenExists_ReturnsOrder() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        Order order = new Order();
        order.setCustomer(userRepository.findById(1L).
                orElseThrow(() -> new NotFoundException("Такого пользователя нет.")));
        order.setPart(savedPart);
        order.setQuantity(10);
        order.setStatus(OrderStatus.NEW);
        Order savedOrder = orderRepository.save(order);

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderService.getOrderById(savedOrder.getId());

        assertEquals(savedOrder.getId(), foundOrder.getId());
        assertEquals(10, foundOrder.getQuantity());
        assertEquals(OrderStatus.NEW, foundOrder.getStatus());
        assertEquals(1L, foundOrder.getCustomer().getId());
        assertEquals(savedPart.getId(), foundOrder.getPart().getId());
        assertNotNull(foundOrder.getCreatedAt());
    }

    @Transactional
    @Test
    void createOrder_WhenValid_SavesOrder() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);
            operations.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        int number = 1;
        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setQuantity(10);
        orderDto.setPartId(savedPart.getId());

        Order savedOrder = orderService.createOrder(orderDto);

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertNotNull(foundOrder.getId());
        assertEquals(1L, foundOrder.getCustomer().getId());
        assertEquals(savedPart.getId(), foundOrder.getPart().getId());
        assertEquals(10, foundOrder.getQuantity());
        assertEquals(OrderStatus.NEW, foundOrder.getStatus());
        assertNotNull(foundOrder.getCreatedAt());

        List<OrderOperation> orderOperations = orderOperationRepository.
                findByOrderIdOrderBySequenceAsc(foundOrder.getId());

        assertEquals(3, orderOperations.size());
        int n = 1;
        for (OrderOperation oo : orderOperations) {
            assertEquals(n, oo.getSequence());
            assertEquals(operations.get(n - 1).getId(), oo.getOperation().getId());
            assertEquals(5, oo.getTimePerPart());
            assertEquals(OrderOperationStatus.PENDING, oo.getStatus());
            if (n == 1) {
                assertEquals(foundOrder.getCreatedAt(), oo.getReadyAt());
            } else {
                assertNull(oo.getReadyAt());
            }
            n++;
        }
    }

    @Transactional
    @Test
    void updateOrder_WhenValid_UpdatesOrder() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);
            operations.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        int number = 1;
        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setQuantity(10);
        orderDto.setPartId(savedPart.getId());

        Order savedOrder = orderService.createOrder(orderDto);

        Part part2 = new Part();
        part2.setName("testPart_ForUpdate");
        part2.setDescription("testDescription_ForUpdate");
        Part savedPart2 = partRepository.save(part2);

        List<Operation> operations2 = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation_ForUpdate" + i);
            operation.setDurationMinutes(10);
            Operation savedOperation = operationRepository.save(operation);
            operations2.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine_ForUpdate" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        number = 1;
        for (Operation operation : operations2) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part2);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        UpdateOrderDto updateOrderDto = new UpdateOrderDto();
        updateOrderDto.setPartId(savedPart2.getId());
        updateOrderDto.setQuantity(15);

        orderService.updateOrder(savedOrder.getId(), updateOrderDto);

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertNotNull(foundOrder.getId());
        assertEquals(1L, foundOrder.getCustomer().getId());
        assertEquals(savedPart2.getId(), foundOrder.getPart().getId());
        assertEquals(15, foundOrder.getQuantity());
        assertEquals(OrderStatus.NEW, foundOrder.getStatus());
        assertNotNull(foundOrder.getCreatedAt());

        List<OrderOperation> orderOperations = orderOperationRepository.
                findByOrderIdOrderBySequenceAsc(foundOrder.getId());

        assertEquals(3, orderOperations.size());
        int n = 1;
        for (OrderOperation oo : orderOperations) {
            assertEquals(n, oo.getSequence());
            assertEquals(operations2.get(n - 1).getId(), oo.getOperation().getId());
            assertEquals(10, oo.getTimePerPart());
            assertEquals(OrderOperationStatus.PENDING, oo.getStatus());
            if (n == 1) {
                assertNotNull(oo.getReadyAt());
            } else {
                assertNull(oo.getReadyAt());
            }
            n++;
        }
    }

    @Transactional
    @Test
    void cancelOrder_WhenNew_CancelOrder() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);
            operations.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        int number = 1;
        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setQuantity(10);
        orderDto.setPartId(savedPart.getId());

        Order savedOrder = orderService.createOrder(orderDto);
        orderService.cancelOrder(savedOrder.getId());

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderService.getOrderById(savedOrder.getId());
        assertEquals(OrderStatus.CANCELLED, foundOrder.getStatus());
        List<OrderOperation> orderOperations = orderOperationRepository.
                findByOrderIdOrderBySequenceAsc(savedOrder.getId());
        assertTrue(orderOperations.isEmpty());
    }


    @Transactional
    @Test
    void updateOrder_WhenInProgress_ThrowsException() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);
            operations.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        int number = 1;
        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setQuantity(10);
        orderDto.setPartId(savedPart.getId());

        Order savedOrder = orderService.createOrder(orderDto);
        savedOrder.setStatus(OrderStatus.IN_PROGRESS);

        entityManager.flush();
        entityManager.clear();

        UpdateOrderDto updateOrderDto = new UpdateOrderDto();
        updateOrderDto.setQuantity(20);
        assertThrows(ConflictException.class, () -> orderService.updateOrder(savedOrder.getId(), updateOrderDto));
    }

    @Transactional
    @Test
    void cancelOrder_WhenInProgress_ThrowsException() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);
            operations.add(savedOperation);
            Machine machine = new Machine();
            machine.setName("testMachine" + i);
            machine.setStatus(MachineStatus.AVAILABLE);
            Machine savedMachine = machineRepository.save(machine);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(savedOperation);
            machineOperation.setMachine(savedMachine);
            machineOperationRepository.save(machineOperation);
        }

        int number = 1;
        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(part);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }

        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setQuantity(10);
        orderDto.setPartId(savedPart.getId());

        Order savedOrder = orderService.createOrder(orderDto);
        savedOrder.setStatus(OrderStatus.IN_PROGRESS);

        entityManager.flush();
        entityManager.clear();

        assertThrows(ConflictException.class, () -> orderService.cancelOrder(savedOrder.getId()));
    }
}
