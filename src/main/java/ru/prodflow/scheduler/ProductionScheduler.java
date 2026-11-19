package ru.prodflow.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.prodflow.service.interfaces.ProductionService;

@Component
@RequiredArgsConstructor
public class ProductionScheduler {

    private final ProductionService productionService;

    @Scheduled(fixedDelay = 5000)
    public void checkProduction() {
        productionService.processProduction();
    }
}
