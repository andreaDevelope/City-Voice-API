package it.cityvoice.api.features.stats;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.stats.dto.PublicStatsResponse;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.ReportCategory;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class PublicStatsIntegrationTest extends IntegrationTestBase {

    @Autowired
    private StoryRepo storyRepo;

    private UUID postStory(TestUser author, StoryType type, ReportCategory category, Long districtId, String title) throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(type, category, districtId, title, "Descrizione", "Contenuto");
        String body = mockMvc.perform(post("/api/cityvoice/stories")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    private PublicStatsResponse fetchStats() throws Exception {
        String body = mockMvc.perform(get("/api/cityvoice/public/stats"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, PublicStatsResponse.class);
    }

    @Test
    @DisplayName("l'accesso anonimo è consentito e la risposta contiene tutti i contatori")
    void anonymousAccessReturnsAllCounters() throws Exception {
        mockMvc.perform(get("/api/cityvoice/public/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").isNumber())
                .andExpect(jsonPath("$.stories").isNumber())
                .andExpect(jsonPath("$.reports").isNumber());
    }

    @Test
    @DisplayName("registrare un utente incrementa i membri di uno")
    void registeringUserIncrementsMembers() throws Exception {
        PublicStatsResponse before = fetchStats();

        registerUser();

        assertEquals(before.members() + 1, fetchStats().members());
    }

    @Test
    @DisplayName("pubblicare una storia incrementa solo le storie")
    void publishingStoryIncrementsOnlyStories() throws Exception {
        TestUser owner = registerUser();
        PublicStatsResponse before = fetchStats();

        postStory(owner, StoryType.STORY, null, null, "Statistiche storia");

        PublicStatsResponse after = fetchStats();
        assertEquals(before.stories() + 1, after.stories());
        assertEquals(before.reports(), after.reports());
    }

    @Test
    @DisplayName("pubblicare una segnalazione incrementa solo le segnalazioni")
    void publishingReportIncrementsOnlyReports() throws Exception {
        TestUser owner = registerUser();
        PublicStatsResponse before = fetchStats();

        postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 13L, "Statistiche segnalazione");

        PublicStatsResponse after = fetchStats();
        assertEquals(before.reports() + 1, after.reports());
        assertEquals(before.stories(), after.stories());
    }

    @Test
    @DisplayName("una segnalazione bloccata non viene contata")
    void blockedReportIsNotCounted() throws Exception {
        TestUser owner = registerUser();
        PublicStatsResponse before = fetchStats();

        UUID blockedId = postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 13L, "Statistiche bloccata");
        Story blocked = storyRepo.findById(blockedId).orElseThrow();
        blocked.setStatus(StoryStatus.BLOCKED);
        storyRepo.save(blocked);

        assertEquals(before.reports(), fetchStats().reports());
    }
}
