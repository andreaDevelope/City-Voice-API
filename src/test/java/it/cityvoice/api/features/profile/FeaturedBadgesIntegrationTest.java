package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.badges.repositories.BadgeRepo;
import it.cityvoice.api.features.profile.user_badge.dto.FeaturedBadgesRequest;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class FeaturedBadgesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private BadgeRepo badgeRepo;

    @Autowired
    private UserBadgeService userBadgeService;

    @Autowired
    private UserBadgeRepo userBadgeRepo;

    private TestUser testUser;

    @BeforeEach
    void setUp() {
        testUser = registerUser();
        unlock(2L);
        unlock(3L);
        unlock(4L);
        unlock(5L);
    }

    private void unlock(Long badgeId) {
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userBadgeService.unlock(userRome, badgeRepo.getReferenceById(badgeId));
    }

    private ResultActions putFeatured(List<Long> badgeIds) throws Exception {
        return mockMvc.perform(put("/api/cityvoice/badge/featured")
                .with(user(testUser.username()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FeaturedBadgesRequest(badgeIds))));
    }

    // posizione letta dal DB, non dalla risposta
    private Integer positionOf(Long badgeId) {
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        return userBadgeRepo.findByUserRomeOrderByUnlockedAtAsc(userRome).stream()
                .filter(userBadge -> userBadge.getBadge().getId().equals(badgeId))
                .findFirst()
                .orElseThrow()
                .getFeaturedPosition();
    }

    @Test
    @DisplayName("i badge sbloccati partono senza posizione")
    void unlockedBadgesStartWithoutPosition() throws Exception {
        mockMvc.perform(get("/api/cityvoice/badge/unlocked").with(user(testUser.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].badgeId", hasItem(2)));

        assertNull(positionOf(2L));
    }

    @Test
    @DisplayName("la posizione segue l'ordine della lista")
    void positionFollowsListOrder() throws Exception {
        putFeatured(List.of(4L, 2L)).andExpect(status().isOk());

        assertEquals(1, positionOf(4L));
        assertEquals(2, positionOf(2L));
        assertNull(positionOf(3L));
    }

    @Test
    @DisplayName("una nuova selezione sostituisce la precedente, anche se un badge resta allo stesso posto")
    void newSelectionReplacesPrevious() throws Exception {
        putFeatured(List.of(2L, 3L, 4L)).andExpect(status().isOk());
        putFeatured(List.of(2L, 5L)).andExpect(status().isOk());

        // il badge 2 resta al posto 1: è il caso che senza clearAutomatically finirebbe a null
        assertEquals(1, positionOf(2L));
        assertEquals(2, positionOf(5L));
        assertNull(positionOf(3L));
        assertNull(positionOf(4L));
    }

    @Test
    @DisplayName("una lista vuota toglie tutti i badge dall'evidenza")
    void emptyListClearsAll() throws Exception {
        putFeatured(List.of(2L, 3L)).andExpect(status().isOk());
        putFeatured(List.of()).andExpect(status().isOk());

        assertNull(positionOf(2L));
        assertNull(positionOf(3L));
    }

    @Test
    @DisplayName("più di tre badge vengono rifiutati")
    void moreThanThreeIsRejected() throws Exception {
        putFeatured(List.of(2L, 3L, 4L, 5L)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("lo stesso badge due volte viene rifiutato")
    void duplicatesAreRejected() throws Exception {
        putFeatured(List.of(2L, 2L)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un badge non sbloccato viene rifiutato")
    void lockedBadgeIsRejected() throws Exception {
        putFeatured(List.of(2L, 25L)).andExpect(status().isBadRequest());
    }
}