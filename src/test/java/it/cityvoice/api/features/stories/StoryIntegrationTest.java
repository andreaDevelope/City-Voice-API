package it.cityvoice.api.features.stories;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.auth.dto.RegisterRequest;
import it.cityvoice.api.features.auth.enums.Role;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.enums.StoryType;
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
                StoryType.REPORT, "decoro", 13L, "Cassonetti pieni",
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
    @DisplayName("due quartieri dello stesso municipio contano come un solo municipio")
    void sameMunicipioCountsOnce() throws Exception {
        // Monti e Trastevere stanno entrambi nel Municipio I
        CreateStoryRequest first = new CreateStoryRequest(
                StoryType.REPORT, "decoro", 1L, "Prima", "Descrizione", "Contenuto");
        CreateStoryRequest second = new CreateStoryRequest(
                StoryType.REPORT, "sicurezza", 13L, "Seconda", "Descrizione", "Contenuto");

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

    @Test
    @WithMockUser(username = "autore")
    @DisplayName("due municipi diversi contano due")
    void differentMunicipiCountTwice() throws Exception {
        // Trastevere (Municipio I) e Garbatella (Municipio VIII)
        CreateStoryRequest first = new CreateStoryRequest(
                StoryType.REPORT, "decoro", 13L, "Prima", "Descrizione", "Contenuto");
        CreateStoryRequest second = new CreateStoryRequest(
                StoryType.REPORT, "sicurezza", 176L, "Seconda", "Descrizione", "Contenuto");

        mockMvc.perform(post("/api/cityvoice/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));
        mockMvc.perform(post("/api/cityvoice/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)));

        UserRome author = userRomeRepo.findByAppUserId(authorAppUserId);
        assertEquals(2, author.getNeighborhoodCounter());
    }

    @Test
    @WithMockUser(username = "autore")
    @DisplayName("una storia senza quartiere incrementa activity ma non neighborhood")
    void storyWithoutDistrictDoesNotCountForNeighborhood() throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(
                StoryType.STORY, null, null, "Titolo", "Descrizione", "Contenuto");

        mockMvc.perform(post("/api/cityvoice/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("STORY"))
                .andExpect(jsonPath("$.category").isEmpty())
                .andExpect(jsonPath("$.district").isEmpty());

        UserRome author = userRomeRepo.findByAppUserId(authorAppUserId);
        assertEquals(1, author.getActivityCounter());
        assertEquals(0, author.getNeighborhoodCounter());
    }
}