package it.cityvoice.api.features.stories;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class MyStoriesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

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

    private void block(UUID storyId) {
        Story story = storyRepo.findById(storyId).orElseThrow();
        story.setStatus(StoryStatus.BLOCKED);
        storyRepo.save(story);
    }

    @Test
    @DisplayName("l'elenco contiene solo i contenuti dell'utente autenticato")
    void myStoriesContainOnlyOwnContent() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        postStory(owner, StoryType.STORY, null, null, "Mia storia elenco");
        postStory(other, StoryType.STORY, null, null, "Storia altrui elenco");

        mockMvc.perform(get("/api/cityvoice/stories/mine").with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Mia storia elenco"));
    }

    @Test
    @DisplayName("l'elenco include i contenuti bloccati ed è ordinato dal più recente")
    void myStoriesIncludeBlockedNewestFirst() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.STORY, null, null, "Mia storia meno recente");
        UUID reportId = postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 13L, "Mia segnalazione più recente");
        block(reportId);

        mockMvc.perform(get("/api/cityvoice/stories/mine").with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].title").value("Mia segnalazione più recente"))
                .andExpect(jsonPath("$.items[0].status").value("BLOCKED"))
                .andExpect(jsonPath("$.items[1].title").value("Mia storia meno recente"))
                .andExpect(jsonPath("$.items[1].status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("il riepilogo conta i contenuti per stato")
    void summaryCountsContentByStatus() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.STORY, null, null, "Riepilogo storia pubblicata");
        UUID reportId = postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 13L, "Riepilogo segnalazione bloccata");
        block(reportId);

        mockMvc.perform(get("/api/cityvoice/stories/mine/summary").with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.published").value(1))
                .andExpect(jsonPath("$.inReview").value(0))
                .andExpect(jsonPath("$.blocked").value(1));
    }

    @Test
    @DisplayName("i municipi del riepilogo coincidono con il contatore neighborhood dell'utente")
    void summaryMunicipiMatchesNeighborhoodCounter() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 13L, "Municipi Trastevere");
        postStory(owner, StoryType.REPORT, ReportCategory.STRADE, 176L, "Municipi Garbatella");

        int neighborhoodCounter = userRomeRepo.findByAppUserId(owner.appUserId()).getNeighborhoodCounter();

        mockMvc.perform(get("/api/cityvoice/stories/mine/summary").with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municipi").value(neighborhoodCounter));
    }

    @Test
    @DisplayName("senza autenticazione l'elenco dei propri contenuti è respinto")
    void anonymousMyStoriesIsRejected() throws Exception {
        // no .with(user(...)): anonymous request
        mockMvc.perform(get("/api/cityvoice/stories/mine"))
                .andExpect(status().isForbidden());
    }
}
