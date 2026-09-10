package it.cityvoice.api.features.profile.user_rome.schedulers;

import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ContinuityResetScheduler {

    private final UserRomeRepo userRomeRepo;

    @Scheduled(cron = "0 0 0 * * MON", zone = "Europe/Rome")
    @Transactional
    public void resetWeeklyContinuity() {
        userRomeRepo.resetAllContinuityCounters();
    }
}