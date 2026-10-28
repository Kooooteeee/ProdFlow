package ru.prodflow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdateMachineDto;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.enums.OrderOperationStatus;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.*;
import ru.prodflow.repository.MachineOperationRepository;
import ru.prodflow.repository.MachineRepository;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.repository.OrderOperationRepository;
import ru.prodflow.service.interfaces.MachineService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private final OperationRepository operationRepository;

    private final MachineRepository machineRepository;

    private final MachineOperationRepository machineOperationRepository;

    private final OrderOperationRepository orderOperationRepository;


    @Transactional
    @Override
    public Machine createMachine(MachineRequestDto machineRequestDto) {
        Long operationId = machineRequestDto.getOperationId();
        Operation operation = operationRepository.
                findById(operationId).
                orElseThrow(() -> new NotFoundException("Операция с id: " + operationId + " не найдена."));

        Machine machine = new Machine();
        machine.setName(machineRequestDto.getName());
        machine.setStatus(MachineStatus.AVAILABLE);

        Machine savedMachine = machineRepository.save(machine);

        MachineOperation machineOperation = new MachineOperation();
        machineOperation.setMachine(savedMachine);
        machineOperation.setOperation(operation);

        machineOperationRepository.save(machineOperation);

        return savedMachine;
    }

    @Transactional
    @Override
    public void updateMachine(Long id, UpdateMachineDto updateMachineDto) {
        Machine machine = getMachineById(id);
        if (!machine.isActive()) throw new ConflictException("Станок неактивен.");

        Operation oldOperation = machineOperationRepository.
                findByMachine_Id(machine.getId()).
                orElseThrow(() -> new NotFoundException("Строка в таблице machine_operations не найдена.")).
                getOperation();

        if (updateMachineDto.getName() != null) machine.setName(updateMachineDto.getName());

        if (updateMachineDto.getOperationId() != null &&
                !Objects.equals(oldOperation.getId(), updateMachineDto.getOperationId())) {

            Long operationId = updateMachineDto.getOperationId();
            Operation operation = operationRepository.
                    findById(operationId).
                    orElseThrow(() -> new NotFoundException("Операция с id: " + operationId + " не найдена."));

            if (machine.getStatus() != MachineStatus.BUSY) {
                List<OrderOperation> orderOperations = orderOperationRepository.
                        findByOperationIdOrderBySequenceAsc(oldOperation.getId()).stream()
                        .filter(orderOperation ->
                                orderOperation.getStatus() != OrderOperationStatus.COMPLETED &&
                                        (orderOperation.getOrder().getStatus() == OrderStatus.NEW ||
                                                orderOperation.getOrder().getStatus() == OrderStatus.IN_PROGRESS))
                        .toList();

                if (!orderOperations.isEmpty()) {
                    List<Machine> availableMachines = machineOperationRepository.
                            findByOperation_Id(oldOperation.getId()).stream()
                            .map(MachineOperation::getMachine)
                            .filter(machine1 ->
                                    machine1.isActive() &&
                                            machine1.getStatus() != MachineStatus.MAINTENANCE &&
                                            !Objects.equals(machine1.getId(), machine.getId()) &&
                                            !machine1.isNeedsMaintenance())
                            .toList();
                    if (availableMachines.isEmpty()) {
                        throw new ConflictException("Старая операция используется, других подходящих станков нет.");
                    }
                }
                machineOperationRepository.deleteByMachine_Id(machine.getId());

                machineOperationRepository.flush();

                MachineOperation machineOperation = new MachineOperation();
                machineOperation.setMachine(machine);
                machineOperation.setOperation(operation);
                machineOperationRepository.save(machineOperation);
            } else {
                throw new ConflictException("Занятому станку нельзя обновить операцию.");
            }
        }
    }

    @Override
    public List<Machine> getMachinesByOperationId(Long id) {
        return machineOperationRepository.findByOperation_Id(id).stream()
                .map(MachineOperation::getMachine)
                .toList();
    }

    @Override
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Станок с id: " + id + " не найден."));
    }

    @Override
    public List<Machine> getMachinesByStatus(MachineStatus status) {
        return machineRepository.findByStatus(status);
    }

    @Transactional
    @Override
    public void archiveMachine(Long id) {
        Machine machine = getMachineById(id);

        if (!machine.isActive()) return;

        if (machine.getStatus() == MachineStatus.BUSY) {
            throw new ConflictException("Занятые станки нельзя архивировать.");
        }

        Operation operation = machineOperationRepository.
                findByMachine_Id(id).
                orElseThrow(() -> new NotFoundException("Строка в таблице machine_operations не найдена.")).
                getOperation();

        List<OrderOperation> orderOperations = orderOperationRepository.
                findByOperationIdOrderBySequenceAsc(operation.getId()).stream()
                .filter(orderOperation ->
                        orderOperation.getStatus() != OrderOperationStatus.COMPLETED &&
                                (orderOperation.getOrder().getStatus() == OrderStatus.NEW ||
                                        orderOperation.getOrder().getStatus() == OrderStatus.IN_PROGRESS))
                .toList();

        if (!orderOperations.isEmpty()) {
            List<Machine> availableMachines = machineOperationRepository.
                    findByOperation_Id(operation.getId()).stream()
                    .map(MachineOperation::getMachine)
                    .filter(machine1 ->
                            machine1.isActive() &&
                                    machine1.getStatus() != MachineStatus.MAINTENANCE &&
                                    !Objects.equals(machine1.getId(), machine.getId()) &&
                                    !machine1.isNeedsMaintenance())
                    .toList();
            if (availableMachines.isEmpty()) {
                throw new ConflictException("Других подходящих станков нет.");
            }
        }
        machine.setActive(false);
    }

    @Transactional
    @Override
    public void restoreMachine(Long id) {
        Machine machine = getMachineById(id);

        if (machine.isActive()) return;

        machine.setActive(true);
    }

    @Transactional
    @Override
    public void makeMachinesBusy(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Machine machine = getMachineById(id);
            if (!(machine.isActive() &&
                    machine.getStatus() == MachineStatus.AVAILABLE &&
                    !machine.isNeedsMaintenance())) {
                throw new ConflictException("Станок с id: " + id + " недоступен.");
            }
            machine.setStatus(MachineStatus.BUSY);
        }
    }

    @Transactional
    @Override
    public void makeMachinesAvailable(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Machine machine = getMachineById(id);
            if (machine.getStatus() != MachineStatus.BUSY) {
                throw new ConflictException("Некорректный статус станка.");
            }
            if (machine.isNeedsMaintenance()) {
                machine.setStatus(MachineStatus.MAINTENANCE);
                machine.setNeedsMaintenance(false);
            } else {
                machine.setStatus(MachineStatus.AVAILABLE);
            }
        }
    }

    @Transactional
    @Override
    public void sendMachineToMaintenance(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Machine machine = getMachineById(id);
            if (!machine.isActive()) {
                throw new ConflictException("Архивный станок нельзя отправить на обслуживание.");
            }
            if (machine.getStatus() == MachineStatus.AVAILABLE) {
                machine.setStatus(MachineStatus.MAINTENANCE);
            } else if (machine.getStatus() == MachineStatus.BUSY) {
                machine.setNeedsMaintenance(true);
            }
        }
    }

    @Transactional
    @Override
    public void completeMaintenance(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Machine machine = getMachineById(id);
            if (!machine.isActive()) {
                throw new ConflictException("Архивный станок нельзя снять с обслуживания.");
            }
            if (machine.getStatus() == MachineStatus.BUSY) {
                throw new ConflictException("Занятый станок не может быть на обслуживании.");
            } else if (machine.getStatus() == MachineStatus.MAINTENANCE) {
                machine.setStatus(MachineStatus.AVAILABLE);
                machine.setNeedsMaintenance(false);
            }
        }
    }
}
