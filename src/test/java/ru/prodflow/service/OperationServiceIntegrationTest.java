package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.model.Operation;
import ru.prodflow.model.Part;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.service.interfaces.MachineService;
import ru.prodflow.service.interfaces.OperationService;
import ru.prodflow.service.interfaces.PartService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OperationServiceIntegrationTest {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private OperationService operationService;

    @Autowired
    private PartService partService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MachineService machineService;

    @Transactional
    @Test
    void archiveOperation_WhenValid_ArchivesOperation() {
        Operation operation = new Operation();
        operation.setName("testOperationName");
        operation.setActive(true);
        operation.setDurationMinutes(5);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machineRequestDto = new MachineRequestDto();
        machineRequestDto.setName("testMachineName");
        machineRequestDto.setOperationId(savedOperation.getId());

        machineService.createMachine(machineRequestDto);

        PartRequestDto partRequestDto = new PartRequestDto();
        partRequestDto.setName("testPartName");
        partRequestDto.setDescription("testDescription");
        partRequestDto.setOperationIds(List.of(savedOperation.getId()));

        Part savedPart = partService.createPart(partRequestDto);
        savedPart.setActive(false);
        entityManager.flush();

        operationService.archiveOperations(List.of(savedOperation.getId()));

        entityManager.flush();
        entityManager.clear();

        Operation foundOperation = operationService.getOperationById(savedOperation.getId());
        assertFalse(foundOperation.isActive());
    }

    @Transactional
    @Test
    void archiveOperation_WhenActivePart_ThrowsException() {
        Operation operation = new Operation();
        operation.setName("testOperationName");
        operation.setActive(true);
        operation.setDurationMinutes(5);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machineRequestDto = new MachineRequestDto();
        machineRequestDto.setName("testMachineName");
        machineRequestDto.setOperationId(savedOperation.getId());

        machineService.createMachine(machineRequestDto);

        PartRequestDto partRequestDto = new PartRequestDto();
        partRequestDto.setName("testPartName");
        partRequestDto.setDescription("testDescription");
        partRequestDto.setOperationIds(List.of(savedOperation.getId()));

        partService.createPart(partRequestDto);

        entityManager.flush();
        entityManager.clear();
        
        assertThrows(ConflictException.class,
                () -> operationService.archiveOperations(List.of(savedOperation.getId())));
    }
}
