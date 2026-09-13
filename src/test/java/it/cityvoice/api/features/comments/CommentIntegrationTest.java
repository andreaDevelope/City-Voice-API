package it.cityvoice.api.features.comments;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.stories.entity.Story;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CommentIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private StoryRepo storyRepo;

    @Autowired
    private CommentRepo commentRepo;

    private TestUser storyOwner;
    private TestUser commenter;
    private TestUser replier;
    private UUID storyId;

    @BeforeEach
    void setUp() {
        // tre utenti distinti: autore storia, primo commentatore, autore delle risposte
        storyOwner = registerUser();
        commenter = registerUser();
        replier = registerUser();

        // storia creata a DB: il flusso di submit è già coperto da StoryIntegrationTest
        Story story = new Story();
        story.setUserRome(userRomeRepo.findByAppUserId(storyOwner.appUserId()));
        story.setCategory("decoro");
        story.setDistrict(FAKER.address().cityName());
        story.setTitle(FAKER.lorem().sentence(3));
        story.setDescription(FAKER.lorem().sentence(5));
        story.setStoryContent(FAKER.lorem().paragraph());
        storyId = storyRepo.save(story).getId();
    }

    // invia un commento come l'utente indicato e restituisce l'id creato
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

    private UserRome reload(TestUser testUser) {
        return userRomeRepo.findByAppUserId(testUser.appUserId());
    }

    @Test
    @DisplayName("un commento diretto dà +2 impact al proprietario della storia")
    void directCommentAwardsStoryOwner() throws Exception {
        // commento di primo livello sulla storia altrui
        postComment(commenter, null);

        assertEquals(2, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("chi commenta guadagna activity e neighborhood, non impact")
    void commenterEarnsActivityAndNeighborhood() throws Exception {
        // l'azione premia l'autore sulle categorie di attività
        postComment(commenter, null);

        UserRome author = reload(commenter);
        assertEquals(1, author.getActivityCounter());
        assertEquals(1, author.getNeighborhoodCounter());
        assertEquals(0, author.getImpactCounter());
    }

    @Test
    @DisplayName("commentare la propria storia non genera impact")
    void selfCommentAwardsNoImpact() throws Exception {
        // il proprietario commenta sé stesso: activity sì, impact no
        postComment(storyOwner, null);

        UserRome owner = reload(storyOwner);
        assertEquals(0, owner.getImpactCounter());
        assertEquals(1, owner.getActivityCounter());
    }

    @Test
    @DisplayName("una risposta dà +2 al destinatario e +1 di bonus al proprietario della storia")
    void nestedReplyAwardsBothOwners() throws Exception {
        // catena a due livelli fra tre utenti diversi
        UUID parentId = postComment(commenter, null);
        postComment(replier, parentId);

        // il destinatario diretto incassa il peso pieno del commento
        assertEquals(2, reload(commenter).getImpactCounter());
        // il proprietario somma i 2 del commento diretto e 1 di bonus
        assertEquals(3, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("oltre la profondità 2 il bonus alla storia non viene più assegnato")
    void bonusStopsAfterMaxDepth() throws Exception {
        // catena di quattro commenti: profondità 0, 1, 2, 3
        UUID depth0 = postComment(commenter, null);
        UUID depth1 = postComment(replier, depth0);
        UUID depth2 = postComment(commenter, depth1);
        postComment(replier, depth2);

        // 2 dal commento diretto, più un bonus per la profondità 1 e uno per la 2
        assertEquals(4, reload(storyOwner).getImpactCounter());
    }

    @Test
    @DisplayName("la profondità viene calcolata dal commento padre")
    void depthIsDerivedFromParent() throws Exception {
        // due livelli annidati, verifica del campo persistito
        UUID parentId = postComment(commenter, null);
        UUID childId = postComment(replier, parentId);

        assertEquals(0, commentRepo.findById(parentId).map(Comment::getDepth).orElseThrow());
        assertEquals(1, commentRepo.findById(childId).map(Comment::getDepth).orElseThrow());
    }
}