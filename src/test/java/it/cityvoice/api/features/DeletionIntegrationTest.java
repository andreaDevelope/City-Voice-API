package it.cityvoice.api.features;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class DeletionIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private StoryRepo storyRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private ReactionRepo reactionRepo;

    private TestUser storyOwner;
    private TestUser commenter;
    private TestUser replier;
    private TestUser reactor;
    private UUID storyId;

    @BeforeEach
    void setUp() throws Exception {
        storyOwner = registerUser();
        commenter = registerUser();
        replier = registerUser();
        reactor = registerUser();
        storyId = postStory(storyOwner);
    }

    private UUID postStory(TestUser author) throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(
                "decoro", FAKER.address().cityName(), FAKER.lorem().sentence(3),
                FAKER.lorem().sentence(5), FAKER.lorem().paragraph());
        String body = mockMvc.perform(post("/api/cityvoice/stories")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    private UUID postComment(TestUser author, UUID parentCommentId) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(storyId, parentCommentId, FAKER.lorem().sentence());
        String body = mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    private void react(TestUser author, UUID targetStoryId, UUID targetCommentId, ReactionType type) throws Exception {
        ReactToContentRequest request = new ReactToContentRequest(targetStoryId, targetCommentId, type);
        mockMvc.perform(post("/api/cityvoice/reactions")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void deleteComment(TestUser author, UUID commentId) throws Exception {
        mockMvc.perform(delete("/api/cityvoice/comments/" + commentId).with(user(author.username())))
                .andExpect(status().isOk());
    }

    private void deleteStory(TestUser author, UUID targetStoryId) throws Exception {
        mockMvc.perform(delete("/api/cityvoice/stories/" + targetStoryId).with(user(author.username())))
                .andExpect(status().isOk());
    }

    private UserRome reload(TestUser testUser) {
        return userRomeRepo.findByAppUserId(testUser.appUserId());
    }

    @Test
    @DisplayName("cancellare un commento revoca solo i punti che ha generato")
    void deletingCommentRevokesOwnPoints() throws Exception {
        // un commento diretto porta il proprietario a 2
        UUID commentId = postComment(commenter, null);
        assertEquals(2, reload(storyOwner).getImpactCounter());

        // cancellandolo il proprietario torna a zero
        deleteComment(commenter, commentId);
        assertEquals(0, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("chi cancella un commento perde activity e neighborhood")
    void deletingCommentCostsActivity() throws Exception {
        UUID commentId = postComment(commenter, null);
        deleteComment(commenter, commentId);

        UserRome author = reload(commenter);
        assertEquals(0, author.getActivityCounter());
        assertEquals(0, author.getNeighborhoodCounter());
    }

    @Test
    @DisplayName("la cascata elimina le risposte ma non toglie punti ai terzi")
    void cascadeKeepsThirdPartyPoints() throws Exception {
        // il rispondente incassa 2 dalla risposta ricevuta sul suo commento
        UUID parentId = postComment(commenter, null);
        postComment(replier, parentId);
        assertEquals(2, reload(commenter).getImpactCounter());

        // cancellando il commento padre spariscono entrambi i commenti
        deleteComment(commenter, parentId);
        assertEquals(0, commentRepo.count());

        // ma il rispondente conserva l'activity guadagnata
        assertEquals(1, reload(replier).getActivityCounter());
    }

    @Test
    @DisplayName("cancellare la storia azzera i punti che ha fruttato al proprietario")
    void deletingStoryRevokesOwnerPoints() throws Exception {
        // un commento diretto e un like portano il proprietario a 3
        postComment(commenter, null);
        react(reactor, storyId, null, ReactionType.LIKE);
        assertEquals(3, reload(storyOwner).getImpactCounter());

        // la cancellazione riporta tutto a zero e svuota le tabelle collegate
        deleteStory(storyOwner, storyId);
        assertEquals(0, reload(storyOwner).getImpactCounter());
        assertEquals(0, commentRepo.count());
        assertEquals(0, reactionRepo.count());
    }

    @Test
    @DisplayName("cancellare la storia non intacca i punti degli altri utenti")
    void deletingStoryKeepsOtherUsersPoints() throws Exception {
        // il commentatore riceve una risposta e arriva a 2
        UUID parentId = postComment(commenter, null);
        postComment(replier, parentId);
        assertEquals(2, reload(commenter).getImpactCounter());

        // dopo la cancellazione della storia i suoi punti restano
        deleteStory(storyOwner, storyId);
        assertEquals(2, reload(commenter).getImpactCounter());
    }

    @Test
    @DisplayName("bug noto: il bonus di una risposta già cancellata resta al proprietario")
    void orphanBonusIsLostOnStoryDeletion() throws Exception {
        // catena a due livelli: il proprietario arriva a 3 (2 dal commento, 1 di bonus)
        UUID parentId = postComment(commenter, null);
        postComment(replier, parentId);
        assertEquals(3, reload(storyOwner).getImpactCounter());

        // il commento intermedio viene cancellato con la sua risposta
        deleteComment(commenter, parentId);

        // cancellando la storia il proprietario dovrebbe tornare a zero
        deleteStory(storyOwner, storyId);
        assertEquals(0, reload(storyOwner).getImpactCounter());
    }
}