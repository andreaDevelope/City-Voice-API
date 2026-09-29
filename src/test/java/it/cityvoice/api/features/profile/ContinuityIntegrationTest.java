package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.profile.user_rome.schedulers.ContinuityResetScheduler;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ContinuityIntegrationTest extends IntegrationTestBase {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final int CONTINUITY_CAP = 7;

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private ContinuityResetScheduler scheduler;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryServ categoryServ;

    @Autowired
    private BadgeServ badgeServ;

    @Autowired
    private UserBadgeRepo userBadgeRepo;

    private TestUser testUser;

    @BeforeEach
    void setUp() {
        testUser = registerUser();
    }

    // simula lo stato lasciato da un accesso passato
    private void setLastAccess(LocalDate date, int streak) {
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userRome.setLastActiveDate(date);
        userRome.setContinuityCounter(streak);
        userRomeRepo.save(userRome);
    }

    // riporta l'ultimo accesso a ieri lasciando la serie intatta: il prossimo
    // accesso vale come giorno consecutivo, senza dover spostare l'orologio
    private void rewindToYesterday() {
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userRome.setLastActiveDate(LocalDate.now(ROME).minusDays(1));
        userRomeRepo.save(userRome);
    }

    // chiamata autenticata qualsiasi: il tracking avviene su ogni lookup dell'utente
    private void access() throws Exception {
        mockMvc.perform(get("/api/cityvoice/badge/progress").with(user(testUser.username())))
                .andExpect(status().isOk());
    }

    private UserRome reload() {
        return userRomeRepo.findByAppUserId(testUser.appUserId());
    }

    @Test
    @DisplayName("un utente appena registrato non ha ancora una data di accesso")
    void freshUserHasNoLastAccess() {
        UserRome userRome = reload();
        assertNull(userRome.getLastActiveDate());
        assertEquals(0, userRome.getContinuityCounter());
    }

    @Test
    @DisplayName("l'accesso di ieri fa salire la serie di uno")
    void consecutiveDayIncrementsStreak() throws Exception {
        // ieri l'utente era a 3 giorni di serie
        setLastAccess(LocalDate.now(ROME).minusDays(1), 3);

        access();

        UserRome userRome = reload();
        assertEquals(4, userRome.getContinuityCounter());
        assertEquals(LocalDate.now(ROME), userRome.getLastActiveDate());
    }

    @Test
    @DisplayName("un buco di due giorni fa ripartire la serie da uno")
    void brokenStreakRestartsAtOne() throws Exception {
        // ultimo accesso tre giorni fa, con una serie lunga
        setLastAccess(LocalDate.now(ROME).minusDays(3), 5);

        access();

        assertEquals(1, reload().getContinuityCounter());
    }

    @Test
    @DisplayName("più accessi nello stesso giorno contano una volta sola")
    void multipleAccessesSameDayCountOnce() throws Exception {
        setLastAccess(LocalDate.now(ROME).minusDays(1), 2);

        access();
        access();
        access();

        assertEquals(3, reload().getContinuityCounter());
    }

    @Test
    @DisplayName("la serie non supera il tetto di sette")
    void streakIsCappedAtSeven() throws Exception {
        // serie già al massimo, accesso consecutivo
        setLastAccess(LocalDate.now(ROME).minusDays(1), 7);

        access();

        assertEquals(7, reload().getContinuityCounter());
    }

    @Test
    @DisplayName("raggiunta la soglia il badge di continuità viene sbloccato")
    void continuityBadgeUnlocksAtThreshold() throws Exception {
        // da 1 a 2: soglia del badge "Giorno 2"
        setLastAccess(LocalDate.now(ROME).minusDays(1), 1);

        access();

        assertEquals(2, reload().getContinuityCounter());

        Category continuity = categoryServ.getCategoryByName("continuity");
        Badge dayTwo = badgeServ.getAllBadgesForCategory(continuity.getId()).stream()
                .filter(badge -> badge.getMissionThreshold() == 2)
                .findFirst()
                .orElseThrow();
        assertTrue(userBadgeRepo.existsByUserRomeAndBadge(reload(), dayTwo));
    }

    @Test
    @DisplayName("il primo accesso in assoluto vale uno")
    void firstAccessCountsAsOne() throws Exception {
        access();

        assertEquals(1, reload().getContinuityCounter());
    }

    @Test
    @DisplayName("sette giorni consecutivi dal primo accesso arrivano al tetto")
    void fullWeekOfAccessesReachesCap() throws Exception {
        // il primo accesso apre la serie: è già il giorno 1
        access();
        assertEquals(1, reload().getContinuityCounter());

        // altri sei giorni consecutivi, uno per iterazione
        for (int day = 2; day <= CONTINUITY_CAP; day++) {
            rewindToYesterday();
            access();

            assertEquals(day, reload().getContinuityCounter());
        }
    }

    @Test
    @DisplayName("il reset del lunedì interrompe la serie iniziata a metà settimana")
    void mondayResetCutsMidWeekStreak() throws Exception {
        // da mercoledì a domenica: cinque accessi consecutivi
        access();
        for (int day = 2; day <= 5; day++) {
            rewindToYesterday();
            access();
        }
        assertEquals(5, reload().getContinuityCounter());

        // lunedì: il job azzera la serie con una bulk update
        scheduler.resetWeeklyContinuity();

        // senza clear, rewindToYesterday salverebbe l'entità in cache con la serie a 5
        entityManager.clear();

        // primo accesso del lunedì, consecutivo alla domenica: riparte da uno
        rewindToYesterday();
        access();

        assertEquals(1, reload().getContinuityCounter());
    }
}