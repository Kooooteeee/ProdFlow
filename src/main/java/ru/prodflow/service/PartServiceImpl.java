package ru.prodflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prodflow.dto.UpdatePartDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.exception.NotFoundException;
import ru.prodflow.model.Part;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.service.interfaces.PartService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartServiceImpl implements PartService {

    private final OperationRepository operationRepository;

    @Override
    public void createPart(PartRequestDto partRequestDto) {

        if (operationRepository.findAll().containsAll(partRequestDto.getOperationIds())) {
            throw new NotFoundException("Некоторые операции не существуют.");
        }

        if (!operationRepository.findByIsActiveTrue().containsAll(partRequestDto.getOperationIds())) {
            throw new ConflictException("Некоторые операции находятся в архиве.");
        }

    }

    @Override
    public Optional<Part> getPartById(Long id) {
        return Optional.empty();
    }

    @Override
    public void updatePart(Long id, UpdatePartDto updatePartDto) {

    }

    @Override
    public void archivePart(Long id) {

    }

    @Override
    public void restorePart(Long id) {

    }

    @Override
    public List<Part> findActive() {
        return List.of();
    }

    @Override
    public List<Part> findAvailableParts() {
        return List.of();
    }
}
