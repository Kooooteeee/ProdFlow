package ru.prodflow.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.prodflow.service.interfaces.ProductionService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "prodflow.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ProductionScheduler {

    private final ProductionService productionService;

    @Scheduled(fixedDelay = 5000)
    public void checkProduction() {
        productionService.processProduction();
    }
}
