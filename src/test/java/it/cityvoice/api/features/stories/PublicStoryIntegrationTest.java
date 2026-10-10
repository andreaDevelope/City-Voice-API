package it.cityvoice.api.features.stories;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class PublicStoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private UserBadgeRepo userBadgeRepo;

    @Autowired
    private StoryRepo storyRepo;

    private UUID postStory(TestUser author, StoryType type, String category, Long districtId,
                            String title, String description, String storyContent) throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(type, category, districtId, title, description, storyContent);
        String body = mockMvc.perform(post("/api/cityvoice/stories")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    private void react(TestUser reactor, UUID storyId, ReactionType type) throws Exception {
        ReactToContentRequest request = new ReactToContentRequest(storyId, null, type);
        mockMvc.perform(post("/api/cityvoice/reactions")
                        .with(user(reactor.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void postComment(TestUser author, UUID storyId, String content) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(storyId, null, content);
        mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("l'accesso anonimo è consentito")
    void anonymousAccessIsAllowed() throws Exception {
        mockMvc.perform(get("/api/cityvoice/public/stories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("le storie sono ordinate dalla più recente")
    void storiesAreOrderedByMostRecent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Prima nel tempo", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Seconda nel tempo", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Seconda nel tempo"))
                .andExpect(jsonPath("$.items[1].title").value("Prima nel tempo"));
    }

    @Test
    @DisplayName("la ricerca per titolo trova il contenuto")
    void searchByTitleFindsContent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Buche pericolose", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "buche"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Buche pericolose"));
    }

    @Test
    @DisplayName("la ricerca per username trova il contenuto")
    void searchByUsernameFindsContent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo autore", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", owner.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Titolo autore"));
    }

    @Test
    @DisplayName("la ricerca per nome del quartiere trova il contenuto")
    void searchByDistrictNameFindsContent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo quartiere", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "Trastevere"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Titolo quartiere"));
    }

    @Test
    @DisplayName("la ricerca per label del municipio trova il contenuto")
    void searchByMunicipioLabelFindsContent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo municipio label", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "Centro Storico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Titolo municipio label"));
    }

    @Test
    @DisplayName("la ricerca per numero del municipio trova il contenuto")
    void searchByMunicipioNumberFindsContent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 176L, "Titolo municipio numero", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "VIII"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Titolo municipio numero"));
    }

    @Test
    @DisplayName("il filtro per categoria restituisce solo le segnalazioni di quella categoria")
    void categoryFilterReturnsOnlyMatchingReports() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Report decoro", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "sicurezza", 13L, "Report sicurezza", "Descrizione", "Contenuto");
        postStory(owner, StoryType.STORY, null, null, "Storia libera", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("category", "decoro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Report decoro"));
    }

    @Test
    @DisplayName("i conteggi di like, dislike e commenti sono corretti")
    void reactionAndCommentCountsAreCorrect() throws Exception {
        TestUser owner = registerUser();
        TestUser liker1 = registerUser();
        TestUser liker2 = registerUser();
        TestUser disliker = registerUser();

        UUID storyId = postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo unico conteggi", "Descrizione", "Contenuto");

        react(liker1, storyId, ReactionType.LIKE);
        react(liker2, storyId, ReactionType.LIKE);
        react(disliker, storyId, ReactionType.DISLIKE);

        postComment(liker1, storyId, "Commento uno");
        postComment(liker2, storyId, "Commento due");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "Titolo unico conteggi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].likes").value(2))
                .andExpect(jsonPath("$.items[0].dislikes").value(1))
                .andExpect(jsonPath("$.items[0].comments").value(2));
    }

    @Test
    @DisplayName("i badge in evidenza dell'autore sono presenti nella risposta")
    void featuredBadgesOfAuthorArePresent() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo badge", "Descrizione", "Contenuto");

        UserRome authorUserRome = userRomeRepo.findByAppUserId(owner.appUserId());
        List<UserBadge> unlocked = userBadgeRepo.findByUserRomeAndBadgeIdIn(authorUserRome, List.of(2L));
        unlocked.get(0).setFeaturedPosition(1);
        userBadgeRepo.save(unlocked.get(0));

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "Titolo badge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].author.featuredBadges.length()").value(1))
                .andExpect(jsonPath("$.items[0].author.featuredBadges[0].id").value(2))
                .andExpect(jsonPath("$.items[0].author.featuredBadges[0].name").value("Er Primo Cinguettio"))
                .andExpect(jsonPath("$.items[0].author.featuredBadges[0].category").value("activity"));
    }

    @Test
    @DisplayName("la paginazione calcola correttamente hasNext")
    void paginationComputesHasNextCorrectly() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Pagina prima", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Pagina seconda", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories").param("size", "1").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(true));

        mockMvc.perform(get("/api/cityvoice/public/stories").param("size", "1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("la preview usa il testo troncato quando manca la descrizione")
    void previewFallsBackToTruncatedContent() throws Exception {
        TestUser owner = registerUser();
        String longContent = "a".repeat(250);
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Titolo preview", null, longContent);

        mockMvc.perform(get("/api/cityvoice/public/stories").param("q", "Titolo preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].preview").value("a".repeat(200) + "…"));
    }

    @Test
    @DisplayName("i conteggi per categoria sono raggruppati e ordinati alfabeticamente")
    void categoryCountsAreGroupedAndOrderedAlphabetically() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Decoro uno", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Decoro due", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "trasporti", 13L, "Trasporti uno", "Descrizione", "Contenuto");
        postStory(owner, StoryType.STORY, null, null, "Storia libera categorie", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("decoro"))
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].category").value("trasporti"))
                .andExpect(jsonPath("$[1].count").value(1));
    }

    @Test
    @DisplayName("categorie con maiuscole diverse sono contate come una sola, in minuscolo")
    void categoryCountsAreCaseInsensitive() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "Decoro", 13L, "Decoro maiuscolo", "Descrizione", "Contenuto");
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Decoro minuscolo", "Descrizione", "Contenuto");

        mockMvc.perform(get("/api/cityvoice/public/stories/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("decoro"))
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    @DisplayName("una segnalazione non pubblicata non viene contata")
    void unpublishedReportIsNotCounted() throws Exception {
        TestUser owner = registerUser();
        postStory(owner, StoryType.REPORT, "decoro", 13L, "Decoro pubblicato", "Descrizione", "Contenuto");
        UUID unpublishedId = postStory(owner, StoryType.REPORT, "decoro", 13L, "Decoro non pubblicato", "Descrizione", "Contenuto");

        Story unpublished = storyRepo.findById(unpublishedId).orElseThrow();
        unpublished.setStatus(StoryStatus.BLOCKED);
        storyRepo.save(unpublished);

        mockMvc.perform(get("/api/cityvoice/public/stories/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("decoro"))
                .andExpect(jsonPath("$[0].count").value(1));
    }
}
