package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdateOperationDto;
import ru.prodflow.dto.request.OperationRequestDto;
import ru.prodflow.model.Operation;

import java.util.List;

public interface OperationService {
    Operation createOperation(OperationRequestDto operationRequestDto);

    void updateOperation(Long id, UpdateOperationDto updateOperationDto);

    Operation getOperationById(Long id);

    void archiveOperations(List<Long> ids);

    void restoreOperations(List<Long> ids);

    List<Operation> getActiveOperations();

    List<Operation> getAllOperations();
}
