package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ContinuityIntegrationTest extends IntegrationTestBase {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Autowired
    private UserRomeRepo userRomeRepo;

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
    @DisplayName("un buco di due giorni azzera la serie")
    void brokenStreakResets() throws Exception {
        // ultimo accesso tre giorni fa, con una serie lunga
        setLastAccess(LocalDate.now(ROME).minusDays(3), 5);

        access();

        assertEquals(0, reload().getContinuityCounter());
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
    }
}