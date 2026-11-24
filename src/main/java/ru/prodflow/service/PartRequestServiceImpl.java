package ru.prodflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.prodflow.dto.UpdatePartRequestDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.dto.request.PartRequestRequestDto;
import ru.prodflow.enums.PartRequestStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.Part;
import ru.prodflow.model.PartRequest;
import ru.prodflow.repository.PartRepository;
import ru.prodflow.repository.PartRequestRepository;
import ru.prodflow.service.interfaces.PartRequestService;
import ru.prodflow.service.interfaces.PartService;
import ru.prodflow.service.interfaces.UserService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PartRequestServiceImpl implements PartRequestService {

    private final UserService userService;

    private final PartRequestRepository partRequestRepository;

    private final PartService partService;

    private final PartRepository partRepository;

    @Transactional
    @Override
    public PartRequest createPartRequest(PartRequestRequestDto partRequestDto, Long customerId) {
        PartRequest partRequest = new PartRequest();
        partRequest.setName(partRequestDto.getName());
        partRequest.setDescription(partRequestDto.getDescription());
        partRequest.setCustomer(userService.findUserById(customerId));
        partRequest.setStatus(PartRequestStatus.PENDING);

        return partRequestRepository.save(partRequest);
    }

    @Transactional
    @Override
    public void approvePartRequest(Long id, List<Long> operationIds) {
        PartRequest partRequest = findPartRequestById(id);

        if (partRequest.getStatus() != PartRequestStatus.PENDING)
            throw new ConflictException("Эта заявка уже обработана.");

        List<Part> prs = partRepository.findAll();
        String name = partRequest.getName().toLowerCase().trim();
        for (Part pr : prs) {
            if (pr.getName().toLowerCase().trim().equals(name)) {
                throw new ConflictException("Деталь с таким названием уже существует. Измените название заявки.");
            }
        }

        PartRequestDto partRequestDto =  new PartRequestDto();
        partRequestDto.setName(partRequest.getName());
        partRequestDto.setDescription(partRequest.getDescription());
        partRequestDto.setOperationIds(operationIds);

        partService.createPart(partRequestDto);

        partRequest.setStatus(PartRequestStatus.APPROVED);
    }

    @Transactional
    @Override
    public void rejectPartRequests(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        for (Long id : uniqueIds) {
            PartRequest pr = findPartRequestById(id);

            if (pr.getStatus() != PartRequestStatus.PENDING) continue;

            pr.setStatus(PartRequestStatus.REJECTED);
        }
    }

    @Transactional
    @Override
    public void updatePartRequest(Long id, UpdatePartRequestDto updatePartRequestDto) {
        PartRequest partRequest = findPartRequestById(id);

        if (partRequest.getStatus() != PartRequestStatus.PENDING)
            throw new ConflictException("Эта заявка уже обработана.");

        if (updatePartRequestDto.getName() != null) partRequest.setName(updatePartRequestDto.getName());
        if (updatePartRequestDto.getDescription() != null) partRequest.
                setDescription(updatePartRequestDto.getDescription());
    }

    @Override
    public List<PartRequest> findPartRequestsByStatus(PartRequestStatus status) {
        return partRequestRepository.findByStatus(status);
    }

    @Override
    public List<PartRequest> findPartRequestByCustomerId(Long id) {
        return partRequestRepository.findByCustomerId(id);
    }

    @Override
    public PartRequest findPartRequestById(Long id) {
        return partRequestRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Запрос с id: " + id + " не найден."));
    }
}
