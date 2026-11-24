package ru.prodflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.model.*;
import ru.prodflow.repository.OrderOperationMachineRepository;
import ru.prodflow.repository.OrderOperationRepository;
import ru.prodflow.service.interfaces.MachineService;
import ru.prodflow.service.interfaces.OrderService;
import ru.prodflow.service.interfaces.ProductionService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService {

    private final OrderOperationRepository orderOperationRepository;

    private final MachineService machineService;

    private final OrderOperationMachineRepository orderOperationMachineRepository;

    private final OrderService orderService;

    @Transactional
    @Override
    public void processProduction() {
        completeOperations();
        completeOrders();
        startOperations();
    }

    private void completeOperations() {
        List<OrderOperation> oos = orderOperationRepository.
                findByStatusAndPlannedEndAtLessThanEqual(OrderOperationStatus.IN_PROGRESS, OffsetDateTime.now());
        for (OrderOperation oo : oos) {
            oo.setStatus(OrderOperationStatus.COMPLETED);
            List<Long> machineIds = orderOperationMachineRepository.findByOrderOperation_Id(oo.getId()).stream()
                    .map(OrderOperationMachine::getMachine)
                    .map(Machine::getId)
                    .toList();
            machineService.makeMachinesAvailable(machineIds);
            int sequence = oo.getSequence();
            orderOperationRepository.findByOrderIdOrderBySequenceAsc(oo.getOrder().getId()).stream()
                    .filter(orderOperation -> orderOperation.getSequence() - 1 == sequence)
                    .findFirst()
                    .ifPresent(orderOperation -> orderOperation.setReadyAt(OffsetDateTime.now()));
        }
    }

    private void completeOrders() {
        List<Order> orders = orderService.findAllByStatus(OrderStatus.IN_PROGRESS);
        for (Order order : orders) {
            List<OrderOperation> oos = orderOperationRepository.findByOrderIdOrderBySequenceAsc(order.getId());
            boolean allCompleted = !oos.isEmpty() && oos.stream().
                    allMatch(oo -> oo.getStatus() == OrderOperationStatus.COMPLETED);
            if (allCompleted) order.setStatus(OrderStatus.COMPLETED);
        }
    }

    private void startOperations() {
        List<OrderOperation> oos = orderOperationRepository.
                findByStatusAndReadyAtLessThanEqualOrderByReadyAtAsc
                        (OrderOperationStatus.PENDING, OffsetDateTime.now());
        for (OrderOperation oo : oos) {
            List<Machine> machines = machineService.getMachinesByOperationId(oo.getOperation().getId()).stream()
                    .filter(machine -> machine.isActive() &&
                            machine.getStatus() == MachineStatus.AVAILABLE &&
                            !machine.isNeedsMaintenance())
                    .toList();
            if (machines.isEmpty()) continue;
            for (Machine machine : machines) {
                OrderOperationMachine orderOperationMachine = new OrderOperationMachine();
                OrderOperationMachineId orderOperationMachineId = new OrderOperationMachineId();
                orderOperationMachineId.setMachineId(machine.getId());
                orderOperationMachineId.setOrderOperationId(oo.getId());
                orderOperationMachine.setId(orderOperationMachineId);
                orderOperationMachine.setMachine(machine);
                orderOperationMachine.setOrderOperation(oo);

                orderOperationMachineRepository.save(orderOperationMachine);
            }
            machineService.makeMachinesBusy(machines.stream().map(Machine::getId).toList());
            oo.setStartedAt(OffsetDateTime.now());
            int machinesCount = machines.size();
            int quantity = oo.getOrder().getQuantity();
            oo.setPlannedEndAt(oo.getStartedAt().
                    plusMinutes((long) quantity / machinesCount * oo.getTimePerPart() +
                            (quantity % machinesCount == 0 ? 0 : oo.getTimePerPart())));
            oo.setStatus(OrderOperationStatus.IN_PROGRESS);
            if (oo.getOrder().getStatus() == OrderStatus.NEW) {
                oo.getOrder().setStatus(OrderStatus.IN_PROGRESS);
            }
        }
    }
}
