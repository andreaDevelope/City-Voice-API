package it.cityvoice.api.features.reactions;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.entity.Reaction;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ReactionIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private ReactionRepo reactionRepo;

    private TestUser storyOwner;
    private TestUser reactor;
    private TestUser otherReactor;
    private UUID storyId;

    @BeforeEach
    void setUp() throws Exception {
        storyOwner = registerUser();
        reactor = registerUser();
        otherReactor = registerUser();
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

    private UserRome reload(TestUser testUser) {
        return userRomeRepo.findByAppUserId(testUser.appUserId());
    }

    @Test
    @DisplayName("un like sulla storia dà +1 impact al proprietario")
    void likeAwardsStoryOwner() throws Exception {
        // reazione positiva sul contenuto altrui
        react(reactor, storyId, null, ReactionType.LIKE);

        assertEquals(1, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("chi mette un like non guadagna nulla")
    void reactingEarnsNothing() throws Exception {
        // reagire non è produzione di contenuto
        react(reactor, storyId, null, ReactionType.LIKE);

        UserRome author = reload(reactor);
        assertEquals(0, author.getActivityCounter());
        assertEquals(0, author.getImpactCounter());
    }

    @Test
    @DisplayName("ripetere la stessa reazione la rimuove e restituisce il punto")
    void repeatingReactionTogglesItOff() throws Exception {
        // primo like: punto assegnato
        react(reactor, storyId, null, ReactionType.LIKE);
        assertEquals(1, reload(storyOwner).getImpactCounter());

        // stessa chiamata: la reazione sparisce e il punto viene revocato
        react(reactor, storyId, null, ReactionType.LIKE);
        assertEquals(0, reload(storyOwner).getImpactCounter());
        assertEquals(0, reactionRepo.count());
    }

    @Test
    @DisplayName("un dislike su un contatore già a zero viene assorbito")
    void dislikeOnZeroIsAbsorbed() throws Exception {
        // il proprietario parte da zero, il dislike non può scendere oltre
        react(reactor, storyId, null, ReactionType.DISLIKE);

        assertEquals(0, reload(storyOwner).getImpactCounter());
        assertEquals(0, reactionRepo.findAll().getFirst().getAppliedDelta());
    }

    @Test
    @DisplayName("rimuovere un dislike assorbito non regala punti")
    void removingAbsorbedDislikeGivesNothing() throws Exception {
        // il dislike non ha tolto nulla, quindi la rimozione non aggiunge nulla
        react(reactor, storyId, null, ReactionType.DISLIKE);
        react(reactor, storyId, null, ReactionType.DISLIKE);

        assertEquals(0, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("un dislike su un contatore positivo toglie davvero un punto")
    void dislikeOnPositiveCounterSubtracts() throws Exception {
        // due like portano il proprietario a 2
        react(reactor, storyId, null, ReactionType.LIKE);
        react(otherReactor, storyId, null, ReactionType.LIKE);
        assertEquals(2, reload(storyOwner).getImpactCounter());

        // il cambio voto di un utente vale -2: annulla il suo like e aggiunge il dislike
        react(reactor, storyId, null, ReactionType.DISLIKE);
        assertEquals(0, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("cambiare voto aggiorna la riga esistente senza duplicarla")
    void switchingVoteUpdatesSameRow() throws Exception {
        react(reactor, storyId, null, ReactionType.DISLIKE);
        react(reactor, storyId, null, ReactionType.LIKE);

        // una sola reazione per utente e contenuto, con il tipo aggiornato
        List<Reaction> reactions = reactionRepo.findAll();
        assertEquals(1, reactions.size());
        assertEquals(ReactionType.LIKE, reactions.getFirst().getType());
    }

    @Test
    @DisplayName("reagire ai propri contenuti non genera punti")
    void selfReactionAwardsNothing() throws Exception {
        // il proprietario mette like a sé stesso
        react(storyOwner, storyId, null, ReactionType.LIKE);

        assertEquals(0, reload(storyOwner).getImpactCounter());
        assertEquals(0, reactionRepo.findAll().getFirst().getAppliedDelta());
    }

    @Test
    @DisplayName("un like su un commento premia l'autore del commento")
    void likeOnCommentAwardsCommentAuthor() throws Exception {
        // il commento porta il proprietario della storia a 2
        UUID commentId = postComment(reactor, null);

        // il like sul commento premia chi lo ha scritto, non il proprietario della storia
        react(otherReactor, null, commentId, ReactionType.LIKE);

        assertEquals(1, reload(reactor).getImpactCounter());
        assertEquals(2, reload(storyOwner).getImpactCounter());
    }
}