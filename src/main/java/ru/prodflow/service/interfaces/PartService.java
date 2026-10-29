package ru.prodflow.service.interfaces;

import ru.prodflow.dto.UpdatePartDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.model.Part;

import java.util.List;

public interface PartService {
    Part createPart(PartRequestDto partRequestDto);

    Part getPartById(Long id);

    void updatePart(Long id, UpdatePartDto updatePartDto);

    void archivePart(Long id);

    void restorePart(Long id);

    List<Part> findActive();

    List<Part> findAvailableParts();
}
