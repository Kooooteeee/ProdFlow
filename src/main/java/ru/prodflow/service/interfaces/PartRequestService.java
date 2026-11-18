package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdatePartRequestDto;
import ru.prodflow.dto.request.PartRequestRequestDto;
import ru.prodflow.enums.PartRequestStatus;
import ru.prodflow.model.PartRequest;

import java.util.List;

public interface PartRequestService {
    PartRequest createPartRequest(PartRequestRequestDto partRequestDto, Long customerId);

    void approvePartRequest(Long id, List<Long> operationIds);

    void rejectPartRequests(List<Long> ids);

    void updatePartRequest(Long id, UpdatePartRequestDto updatePartRequestDto);

    List<PartRequest> findPartRequestsByStatus(PartRequestStatus status);

    List<PartRequest> findPartRequestByCustomerId(Long id);

    PartRequest findPartRequestById(Long id);
}
