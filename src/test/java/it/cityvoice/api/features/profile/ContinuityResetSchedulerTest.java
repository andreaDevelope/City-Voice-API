package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.profile.user_rome.schedulers.ContinuityResetScheduler;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class ContinuityResetSchedulerTest extends IntegrationTestBase {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private ContinuityResetScheduler scheduler;

    @Autowired
    private EntityManager entityManager;

    // porta un utente a una serie di continuità già avviata
    private Long userWithStreak(int streak) {
        TestUser testUser = registerUser();
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userRome.setContinuityCounter(streak);
        userRome.setLastActiveDate(LocalDate.now(ROME));
        userRomeRepo.save(userRome);
        return testUser.appUserId();
    }

    @Test
    @DisplayName("il reset settimanale azzera la serie di tutti gli utenti")
    void weeklyResetClearsEveryStreak() {
        Long firstId = userWithStreak(5);
        Long secondId = userWithStreak(7);

        scheduler.resetWeeklyContinuity();

        // la query di bulk update non aggiorna le entità già in sessione
        entityManager.clear();

        assertEquals(0, userRomeRepo.findByAppUserId(firstId).getContinuityCounter());
        assertEquals(0, userRomeRepo.findByAppUserId(secondId).getContinuityCounter());
    }

    @Test
    @DisplayName("il reset non tocca gli altri contatori")
    void weeklyResetLeavesOtherCountersUntouched() {
        TestUser testUser = registerUser();
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userRome.setContinuityCounter(4);
        userRome.setActivityCounter(9);
        userRome.setImpactCounter(12);
        userRome.setNeighborhoodCounter(3);
        userRomeRepo.save(userRome);

        // eseguo il job: una bulk update che azzera la colonna direttamente
        // nel database. Le righe cambiano, ma gli oggetti in cache no.
        scheduler.resetWeeklyContinuity();

        // svuoto la cache: obbligo Hibernate a dimenticare quello che ha in memoria.
        entityManager.clear();

        UserRome after = userRomeRepo.findByAppUserId(testUser.appUserId());
        assertEquals(0, after.getContinuityCounter());
        assertEquals(9, after.getActivityCounter());
        assertEquals(12, after.getImpactCounter());
        assertEquals(3, after.getNeighborhoodCounter());
    }
}