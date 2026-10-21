package ru.prodflow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdatePartDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.enums.MachineStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.Operation;
import ru.prodflow.model.Part;
import ru.prodflow.model.PartOperation;
import ru.prodflow.model.PartOperationId;
import ru.prodflow.repository.MachineOperationRepository;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.repository.PartOperationRepository;
import ru.prodflow.repository.PartRepository;
import ru.prodflow.service.interfaces.PartService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final OperationRepository operationRepository;
    private final MachineOperationRepository machineOperationRepository;
    private final PartOperationRepository partOperationRepository;

    @Transactional
    @Override
    public void createPart(PartRequestDto partRequestDto) {
        List<Operation> operations = getCheckedIds(partRequestDto.getOperationIds());
        Part part = new Part();
        part.setName(partRequestDto.getName());
        part.setDescription(partRequestDto.getDescription());

        Part savedPart = partRepository.save(part);

        int number = 1;

        for (Operation operation : operations) {
            PartOperation partOperation = new PartOperation();
            partOperation.setPart(savedPart);
            partOperation.setOperation(operation);

            PartOperationId partOperationId = new PartOperationId();
            partOperationId.setSequence(number);

            partOperation.setId(partOperationId);

            partOperationRepository.save(partOperation);

            number++;
        }
    }

    @Override
    public Part getPartById(Long id) {
        return partRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Деталь с id: " + id + " не существует."));
    }

    @Transactional
    @Override
    public void updatePart(Long partId, UpdatePartDto updatePartDto) {
        Part part = getPartById(partId);
        if (updatePartDto.getName() != null) part.setName(updatePartDto.getName());
        if (updatePartDto.getDescription() != null) part.setDescription(updatePartDto.getDescription());
        if (updatePartDto.getOperationIds() != null) {

            List<Operation> operations = getCheckedIds(updatePartDto.getOperationIds());

            partOperationRepository.deleteAllByPart_id(partId);
            partOperationRepository.flush();

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
        }
    }

    @Transactional
    @Override
    public void archivePart(Long id) {
        Part part = getPartById(id);
        part.setActive(false);
    }

    @Transactional
    @Override
    public void restorePart(Long id) {
        Part part = getPartById(id);
        if (partOperationRepository.existsByPart_IdAndOperation_IsActiveFalse(id)) {
            throw new ConflictException("Одна или несколько операций находятся в архиве.");
        }
        part.setActive(true);
    }

    @Override
    public List<Part> findActive() {
        return partRepository.findByIsActiveTrue();
    }

    @Transactional
    @Override
    public List<Part> findAvailableParts() {
        List<Part> result = new ArrayList<>();
        for (Part part : findActive()) {
            boolean isAvailable = true;
            List<Operation> operations =
                    partOperationRepository.findAllByPart_IdOrderById_SequenceAsc(part.getId()).stream()
                            .map(PartOperation::getOperation)
                            .toList();

            for (Operation operation : operations) {
                if (!operation.isActive()) {
                    isAvailable = false;
                    break;
                }
                if (!machineOperationRepository.
                        existsByMachine_IsActiveTrueAndMachine_NeedsMaintenanceFalseAndOperation_IdAndMachine_StatusNot
                                (operation.getId(), MachineStatus.MAINTENANCE)) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable && !operations.isEmpty()) result.add(part);
        }
        return result;
    }

    private List<Operation> getCheckedIds(List<Long> ids) {
        List<Operation> operations = new ArrayList<>();
        for (Long id : ids) {

            Operation operation = operationRepository.findById(id).orElseThrow(() ->
                    new NotFoundException("Операция с id: " + id + " не существует."));

            if (!operation.isActive()) {
                throw new ConflictException("Операция с id: " + id + " находится в архиве.");
            }

            if (!machineOperationRepository.
                    existsByMachine_IsActiveTrueAndMachine_NeedsMaintenanceFalseAndOperation_IdAndMachine_StatusNot
                            (id, MachineStatus.MAINTENANCE)) {
                throw new ConflictException
                        ("Операция с id: " + id + " недоступна по причине недоступности станка.");
            }
            operations.add(operation);
        }
        return operations;
    }
}
