package it.cityvoice.api.features.stories;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.auth.dto.RegisterRequest;
import it.cityvoice.api.features.auth.enums.Role;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class StoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private UserRomeRepo userRomeRepo;

    private Long authorAppUserId;

    @BeforeEach
    void setUp() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("autore");
        request.setPassword("password1");
        authorAppUserId = appUserService.registerUser(request, Set.of(Role.ROLE_USER)).user().getId();
    }

    @Test
    @WithMockUser(username = "autore")
    @DisplayName("l'invio di una storia incrementa activity e neighborhood")
    void submitStoryIncrementsCounters() throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(
                "decoro", "Trastevere", "Cassonetti pieni",
                "Rifiuti a terra da giorni", "Sono pieni da una settimana");

        mockMvc.perform(post("/api/cityvoice/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.district").value("Trastevere"));

        UserRome author = userRomeRepo.findByAppUserId(authorAppUserId);
        assertEquals(1, author.getActivityCounter());
        assertEquals(1, author.getNeighborhoodCounter());
        assertEquals(0, author.getImpactCounter());
    }

    @Test
    @WithMockUser(username = "autore")
    @DisplayName("due storie nello stesso quartiere contano come un solo quartiere")
    void sameDistrictCountsOnce() throws Exception {
        CreateStoryRequest first = new CreateStoryRequest(
                "decoro", "Trastevere", "Prima", "Descrizione", "Contenuto");
        CreateStoryRequest second = new CreateStoryRequest(
                "sicurezza", "Trastevere", "Seconda", "Descrizione", "Contenuto");

        mockMvc.perform(post("/api/cityvoice/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));
        mockMvc.perform(post("/api/cityvoice/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)));

        UserRome author = userRomeRepo.findByAppUserId(authorAppUserId);
        assertEquals(2, author.getActivityCounter());
        assertEquals(1, author.getNeighborhoodCounter());
    }
}