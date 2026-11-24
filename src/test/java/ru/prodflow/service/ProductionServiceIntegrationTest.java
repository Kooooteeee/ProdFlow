package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.model.*;
import ru.prodflow.repository.*;
import ru.prodflow.service.interfaces.MachineService;
import ru.prodflow.service.interfaces.OrderService;
import ru.prodflow.service.interfaces.ProductionService;
import ru.prodflow.service.interfaces.UserService;

import java.util.ArrayList;
import java.util.List;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductionServiceIntegrationTest {

    @Autowired
    private ProductionService productionService;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private MachineService machineService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private MachineOperationRepository machineOperationRepository;

    @Autowired
    private PartOperationRepository partOperationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderOperationRepository orderOperationRepository;

    @Autowired
    private OrderOperationMachineRepository orderOperationMachineRepository;

    @Transactional
    @Test
    public void startOperations_WhenValid_StartsOperations() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        List<Operation> operations = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            Operation o = new Operation();
            o.setName("testOperation" + i);
            o.setDurationMinutes(5);
            Operation so = operationRepository.save(o);
            operations.add(so);
            Machine m = new Machine();
            m.setName("testMachine" + i);
            m.setStatus(MachineStatus.AVAILABLE);
            Machine sm = machineRepository.save(m);
            MachineOperation machineOperation = new MachineOperation();
            machineOperation.setOperation(so);
            machineOperation.setMachine(sm);
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

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test2@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        Order savedOrder = orderService.createOrder(orderDto, savedUser.getId());

        entityManager.flush();
        entityManager.clear();

        productionService.processProduction();

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderService.getOrderById(savedOrder.getId());
        assertEquals(OrderStatus.IN_PROGRESS, foundOrder.getStatus());

        List<OrderOperation> orderOperations = orderOperationRepository.
                findByOrderIdOrderBySequenceAsc(foundOrder.getId());

        assertEquals(3, orderOperations.size());

        OrderOperation orderOperation = orderOperations.getFirst();

        assertEquals(OrderOperationStatus.IN_PROGRESS, orderOperation.getStatus());

        assertNotNull(orderOperation.getStartedAt());

        assertNotNull(orderOperation.getPlannedEndAt());

        assertEquals(orderOperation.getPlannedEndAt(), orderOperation.getStartedAt().plusMinutes(50));

        List<OrderOperationMachine> orderOperationMachines = orderOperationMachineRepository.
                findByOrderOperation_Id(orderOperation.getId());

        assertEquals(1, orderOperationMachines.size());

        Machine machine = orderOperationMachines.getFirst().getMachine();

        assertEquals(MachineStatus.BUSY, machine.getStatus());

        for (OrderOperation oo : orderOperations) {
            if (oo.getSequence() == 1) continue;
            List<OrderOperationMachine> oom = orderOperationMachineRepository.
                    findByOrderOperation_Id(oo.getId());
            assertTrue(oom.isEmpty());

            assertEquals(OrderOperationStatus.PENDING, oo.getStatus());

            assertNull(oo.getStartedAt());

            assertNull(oo.getPlannedEndAt());

            assertNull(oo.getReadyAt());
        }
    }


    @Transactional
    @Test
    public void completeOperations_WhenTimeExpired_CompletesOperation() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        Operation firstOperation = new Operation();
        firstOperation.setName("testOperation1");
        firstOperation.setDurationMinutes(5);
        Operation savedFirstOperation = operationRepository.save(firstOperation);

        Operation secondOperation = new Operation();
        secondOperation.setName("testOperation2");
        secondOperation.setDurationMinutes(10);
        Operation savedSecondOperation = operationRepository.save(secondOperation);

        Machine firstMachine = new Machine();
        firstMachine.setName("testMachine1");
        firstMachine.setStatus(MachineStatus.BUSY);
        Machine savedFirstMachine = machineRepository.save(firstMachine);

        Machine secondMachine = new Machine();
        secondMachine.setName("testMachine2");
        secondMachine.setStatus(MachineStatus.MAINTENANCE);
        Machine savedSecondMachine = machineRepository.save(secondMachine);

        MachineOperation firstMachineOperation = new MachineOperation();
        firstMachineOperation.setOperation(savedFirstOperation);
        firstMachineOperation.setMachine(savedFirstMachine);
        machineOperationRepository.save(firstMachineOperation);

        MachineOperation secondMachineOperation = new MachineOperation();
        secondMachineOperation.setOperation(savedSecondOperation);
        secondMachineOperation.setMachine(savedSecondMachine);
        machineOperationRepository.save(secondMachineOperation);

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("complete-operation@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        Order order = new Order();
        order.setCustomer(savedUser);
        order.setPart(savedPart);
        order.setQuantity(10);
        order.setStatus(OrderStatus.IN_PROGRESS);
        entityManager.persist(order);

        OffsetDateTime startedAt = OffsetDateTime.now().minusMinutes(60);

        OrderOperation firstOo = new OrderOperation();
        firstOo.setOrder(order);
        firstOo.setOperation(savedFirstOperation);
        firstOo.setSequence(1);
        firstOo.setTimePerPart(5);
        firstOo.setStatus(OrderOperationStatus.IN_PROGRESS);
        firstOo.setReadyAt(startedAt);
        firstOo.setStartedAt(startedAt);
        firstOo.setPlannedEndAt(startedAt.plusMinutes(50));
        OrderOperation savedFirstOo = orderOperationRepository.save(firstOo);

        OrderOperation secondOo = new OrderOperation();
        secondOo.setOrder(order);
        secondOo.setOperation(savedSecondOperation);
        secondOo.setSequence(2);
        secondOo.setTimePerPart(10);
        secondOo.setStatus(OrderOperationStatus.PENDING);
        OrderOperation savedSecondOo = orderOperationRepository.save(secondOo);

        OrderOperationMachine orderOperationMachine = new OrderOperationMachine();
        OrderOperationMachineId id = new OrderOperationMachineId();
        id.setOrderOperationId(savedFirstOo.getId());
        id.setMachineId(savedFirstMachine.getId());

        orderOperationMachine.setId(id);
        orderOperationMachine.setOrderOperation(savedFirstOo);
        orderOperationMachine.setMachine(savedFirstMachine);
        orderOperationMachineRepository.save(orderOperationMachine);

        entityManager.flush();
        entityManager.clear();

        productionService.processProduction();

        entityManager.flush();
        entityManager.clear();

        OrderOperation foundFirstOo = orderOperationRepository
                .findById(savedFirstOo.getId()).orElseThrow();

        OrderOperation foundSecondOo = orderOperationRepository
                .findById(savedSecondOo.getId()).orElseThrow();

        assertEquals(OrderOperationStatus.COMPLETED, foundFirstOo.getStatus());

        assertEquals(OrderOperationStatus.PENDING, foundSecondOo.getStatus());

        assertNotNull(foundSecondOo.getReadyAt());
        assertNull(foundSecondOo.getStartedAt());
        assertNull(foundSecondOo.getPlannedEndAt());

        assertEquals(MachineStatus.AVAILABLE,
                machineService.getMachineById(savedFirstMachine.getId()).getStatus());

        assertEquals(MachineStatus.MAINTENANCE,
                machineService.getMachineById(savedSecondMachine.getId()).getStatus());

        assertEquals(1, orderOperationMachineRepository
                .findByOrderOperation_Id(savedFirstOo.getId()).size());

        assertTrue(orderOperationMachineRepository
                .findByOrderOperation_Id(savedSecondOo.getId()).isEmpty());

        assertEquals(OrderStatus.IN_PROGRESS,
                orderService.getOrderById(order.getId()).getStatus());
    }


    @Transactional
    @Test
    public void completeOrders_WhenAllOperationsCompleted_CompletesOrder() {
        Part part = new Part();
        part.setName("testPart");
        part.setDescription("testDescription");
        Part savedPart = partRepository.save(part);

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("complete-order@example.com");
        userRequestDto.setName("testName");
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);

        Order order = new Order();
        order.setCustomer(savedUser);
        order.setPart(savedPart);
        order.setQuantity(10);
        order.setStatus(OrderStatus.IN_PROGRESS);
        entityManager.persist(order);

        List<OrderOperation> orderOperations = new ArrayList<>();

        for (int i = 1; i < 3; i++) {
            Operation operation = new Operation();
            operation.setName("testOperation" + i);
            operation.setDurationMinutes(5);
            Operation savedOperation = operationRepository.save(operation);

            OrderOperation oo = new OrderOperation();
            oo.setOrder(order);
            oo.setOperation(savedOperation);
            oo.setSequence(i);
            oo.setTimePerPart(5);
            oo.setStatus(OrderOperationStatus.COMPLETED);
            oo.setStartedAt(OffsetDateTime.now().minusMinutes(60));
            oo.setPlannedEndAt(OffsetDateTime.now().minusMinutes(10));

            OrderOperation savedOo = orderOperationRepository.save(oo);
            orderOperations.add(savedOo);
        }

        entityManager.flush();
        entityManager.clear();

        productionService.processProduction();

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderService.getOrderById(order.getId());

        assertEquals(OrderStatus.COMPLETED, foundOrder.getStatus());

        List<OrderOperation> foundOperations = orderOperationRepository
                .findByOrderIdOrderBySequenceAsc(foundOrder.getId());

        assertEquals(2, foundOperations.size());

        assertTrue(foundOperations.stream()
                .allMatch(oo -> oo.getStatus() == OrderOperationStatus.COMPLETED));
    }
}
