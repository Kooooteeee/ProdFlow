package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.prodflow.dto.UpdateMachineDto;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.model.*;
import ru.prodflow.repository.MachineOperationRepository;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.repository.OrderOperationRepository;
import ru.prodflow.service.interfaces.MachineService;
import ru.prodflow.service.interfaces.OrderService;
import ru.prodflow.service.interfaces.PartService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MachineServiceIntegrationTest {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private MachineService machineService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MachineOperationRepository machineOperationRepository;

    @Autowired
    private PartService partService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderOperationRepository orderOperationRepository;

    @Transactional
    @Test
    void createMachine_WhenValid_SavesMachine() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        entityManager.flush();
        entityManager.clear();

        Machine foundMachine = machineService.getMachineById(savedMachine.getId());

        assertEquals(savedMachine.getId(), foundMachine.getId());
        assertEquals("testMachine", foundMachine.getName());
        assertEquals(MachineStatus.AVAILABLE, foundMachine.getStatus());
        assertTrue(foundMachine.isActive());
        assertFalse(foundMachine.isNeedsMaintenance());

        MachineOperation machineOperation = machineOperationRepository.
                findByMachine_Id(foundMachine.getId()).orElseThrow();

        assertEquals(savedOperation.getId(), machineOperation.getOperation().getId());
    }

    @Transactional
    @Test
    void updateMachine_WhenValid_UpdatesMachine() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        Operation updatedOperation = new Operation();
        updatedOperation.setName("testOperation_Updated");
        updatedOperation.setDurationMinutes(15);

        Operation savedUpdatedOperation = operationRepository.save(updatedOperation);

        UpdateMachineDto updateMachineDto = new UpdateMachineDto();
        updateMachineDto.setName("testMachine_Updated");
        updateMachineDto.setOperationId(savedUpdatedOperation.getId());

        machineService.updateMachine(savedMachine.getId(), updateMachineDto);

        entityManager.flush();
        entityManager.clear();

        Machine foundUpdatedMachine = machineService.getMachineById(savedMachine.getId());

        assertEquals("testMachine_Updated", foundUpdatedMachine.getName());
        assertEquals("testOperation_Updated",
                machineOperationRepository.findByMachine_Id(foundUpdatedMachine.getId()).orElseThrow().
                        getOperation().getName());

    }

    @Transactional
    @Test
    void updateMachine_WhenBusy_ThrowsException() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        List<Long> ids = new ArrayList<>();
        ids.add(savedMachine.getId());

        machineService.makeMachinesBusy(ids);

        Operation updatedOperation = new Operation();
        updatedOperation.setName("testOperation_Updated");
        updatedOperation.setDurationMinutes(15);

        Operation savedUpdatedOperation = operationRepository.save(updatedOperation);

        UpdateMachineDto updateMachineDto = new UpdateMachineDto();
        updateMachineDto.setName("testMachine_Updated");
        updateMachineDto.setOperationId(savedUpdatedOperation.getId());

        assertThrows(ConflictException.class,
                () -> machineService.updateMachine(savedMachine.getId(), updateMachineDto));
    }

    @Transactional
    @Test
    void updateMachine_WhenNoAlternativeMachine_ThrowsException() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        PartRequestDto partRequestDto = new PartRequestDto();
        partRequestDto.setName("testPart");
        partRequestDto.setDescription("testDescription");
        partRequestDto.setOperationIds(List.of(savedOperation.getId()));

        Part savedPart = partService.createPart(partRequestDto);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setPartId(savedPart.getId());
        orderRequestDto.setQuantity(10);

        orderService.createOrder(orderRequestDto);

        Operation updatedOperation = new Operation();
        updatedOperation.setName("testOperation_Updated");
        updatedOperation.setDurationMinutes(15);

        Operation savedUpdatedOperation = operationRepository.save(updatedOperation);

        UpdateMachineDto updateMachineDto = new UpdateMachineDto();
        updateMachineDto.setName("testMachine_Updated");
        updateMachineDto.setOperationId(savedUpdatedOperation.getId());

        assertThrows(ConflictException.class,
                () -> machineService.updateMachine(savedMachine.getId(), updateMachineDto));
    }

    @Transactional
    @Test
    void archiveMachine_WhenValid_ArchivesMachine() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        machineService.archiveMachine(savedMachine.getId());

        entityManager.flush();
        entityManager.clear();

        Machine foundArchivedMachine = machineService.getMachineById(savedMachine.getId());
        assertFalse(foundArchivedMachine.isActive());
    }

    @Transactional
    @Test
    void archiveMachine_WhenBusy_ThrowsException() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        List<Long> ids = new ArrayList<>();
        ids.add(savedMachine.getId());

        machineService.makeMachinesBusy(ids);

        assertThrows(ConflictException.class, () -> machineService.archiveMachine(savedMachine.getId()));
    }

    @Transactional
    @Test
    void archiveMachine_WhenNoAlternativeMachine_ThrowsException() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        PartRequestDto partRequestDto = new PartRequestDto();
        partRequestDto.setName("testPart");
        partRequestDto.setDescription("testDescription");
        partRequestDto.setOperationIds(List.of(savedOperation.getId()));

        Part savedPart = partService.createPart(partRequestDto);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setPartId(savedPart.getId());
        orderRequestDto.setQuantity(10);

        orderService.createOrder(orderRequestDto);

        assertThrows(ConflictException.class, () -> machineService.archiveMachine(savedMachine.getId()));
    }

    @Transactional
    @Test
    void checkMachineMaintenanceCycle() {
        Operation operation = new Operation();
        operation.setName("testOperation");
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machine = new MachineRequestDto();
        machine.setName("testMachine");
        machine.setOperationId(savedOperation.getId());

        Machine savedMachine = machineService.createMachine(machine);

        List<Long> ids = new ArrayList<>();
        ids.add(savedMachine.getId());

        machineService.makeMachinesBusy(ids);

        entityManager.flush();
        entityManager.clear();

        Machine foundMachine = machineService.getMachineById(savedMachine.getId());

        assertEquals(MachineStatus.BUSY, foundMachine.getStatus());

        machineService.sendMachineToMaintenance(List.of(foundMachine.getId()));

        entityManager.flush();
        entityManager.clear();

        Machine foundMachine1 = machineService.getMachineById(savedMachine.getId());

        assertEquals(MachineStatus.BUSY, foundMachine1.getStatus());
        assertTrue(foundMachine1.isNeedsMaintenance());

        machineService.makeMachinesAvailable(List.of(savedMachine.getId()));

        entityManager.flush();
        entityManager.clear();

        Machine foundMachine2 = machineService.getMachineById(savedMachine.getId());

        assertEquals(MachineStatus.MAINTENANCE, foundMachine2.getStatus());
        assertFalse(foundMachine2.isNeedsMaintenance());

        machineService.completeMaintenance(List.of(savedMachine.getId()));

        entityManager.flush();
        entityManager.clear();

        Machine foundMachine3 = machineService.getMachineById(savedMachine.getId());

        assertEquals(MachineStatus.AVAILABLE, foundMachine3.getStatus());
        assertFalse(foundMachine3.isNeedsMaintenance());
    }
}
