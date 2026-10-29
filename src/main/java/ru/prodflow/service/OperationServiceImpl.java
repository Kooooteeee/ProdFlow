package ru.prodflow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdateOperationDto;
import ru.prodflow.dto.request.OperationRequestDto;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.Operation;
import ru.prodflow.model.Part;
import ru.prodflow.model.PartOperation;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.repository.PartOperationRepository;
import ru.prodflow.service.interfaces.OperationService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;

    private final PartOperationRepository partOperationRepository;

    @Transactional
    @Override
    public Operation createOperation(OperationRequestDto operationRequestDto) {
        Operation operation = new Operation();
        operation.setName(operationRequestDto.getName());
        operation.setDurationMinutes(operationRequestDto.getDurationMinutes());

        return operationRepository.save(operation);
    }

    @Transactional
    @Override
    public void updateOperation(Long id, UpdateOperationDto updateOperationDto) {
        Operation operation = getOperationById(id);

        if (updateOperationDto.getName() != null) operation.setName(updateOperationDto.getName());
        if (updateOperationDto.getDurationMinutes() != null) operation.
                setDurationMinutes(updateOperationDto.getDurationMinutes());
    }

    @Override
    public Operation getOperationById(Long id) {
        return operationRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Операция с id: " + id + " не найдена."));
    }

    @Transactional
    @Override
    public void archiveOperations(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Operation operation = getOperationById(id);
            if (!operation.isActive()) continue;

            List<Part> parts = partOperationRepository.findAllByOperation_Id(id).stream()
                    .map(PartOperation::getPart)
                    .filter(Part::isActive)
                    .toList();
            if (!parts.isEmpty())
                throw new ConflictException("Нельзя архивировать операцию, " +
                        "участвующую в технологических маршрутах активных деталей. id операции: " + id +".");

            operation.setActive(false);
        }
    }

    @Transactional
    @Override
    public void restoreOperations(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            Operation operation = getOperationById(id);
            if (operation.isActive()) continue;

            operation.setActive(true);
        }
    }

    @Override
    public List<Operation> getActiveOperations() {
        return operationRepository.findByIsActiveTrue();
    }

    @Override
    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }
}
