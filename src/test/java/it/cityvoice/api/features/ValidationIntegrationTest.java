package it.cityvoice.api.features;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ValidationIntegrationTest extends IntegrationTestBase {

    private TestUser owner;
    private TestUser otherUser;
    private UUID storyId;
    private UUID commentId;

    @BeforeEach
    void setUp() throws Exception {
        owner = registerUser();
        otherUser = registerUser();
        storyId = postStory(owner);
        commentId = postComment(owner, storyId, null);
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

    private UUID postComment(TestUser author, UUID targetStoryId, UUID parentCommentId) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(targetStoryId, parentCommentId, FAKER.lorem().sentence());
        String body = mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(author.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    @Test
    @DisplayName("una reazione senza target viene rifiutata")
    void reactionWithoutTargetIsRejected() throws Exception {
        // né storia né commento indicati
        ReactToContentRequest request = new ReactToContentRequest(null, null, ReactionType.LIKE);

        mockMvc.perform(post("/api/cityvoice/reactions")
                        .with(user(otherUser.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("una reazione con due target viene rifiutata")
    void reactionWithBothTargetsIsRejected() throws Exception {
        // storia e commento indicati insieme
        ReactToContentRequest request = new ReactToContentRequest(storyId, commentId, ReactionType.LIKE);

        mockMvc.perform(post("/api/cityvoice/reactions")
                        .with(user(otherUser.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un commento con testo vuoto viene rifiutato")
    void blankCommentIsRejected() throws Exception {
        // il contenuto è obbligatorio
        CreateCommentRequest request = new CreateCommentRequest(storyId, null, "   ");

        mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(otherUser.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("rispondere a un commento di un'altra storia viene rifiutato")
    void parentCommentFromAnotherStoryIsRejected() throws Exception {
        // seconda storia con un suo commento, usato come padre sulla prima
        UUID otherStoryId = postStory(otherUser);
        UUID foreignCommentId = postComment(otherUser, otherStoryId, null);

        CreateCommentRequest request = new CreateCommentRequest(storyId, foreignCommentId, FAKER.lorem().sentence());

        mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(otherUser.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("commentare una storia inesistente restituisce 404")
    void commentOnMissingStoryReturnsNotFound() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(UUID.randomUUID(), null, FAKER.lorem().sentence());

        mockMvc.perform(post("/api/cityvoice/comments")
                        .with(user(otherUser.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cancellare il commento di un altro utente è vietato")
    void deletingSomeoneElsesCommentIsForbidden() throws Exception {
        // il commento appartiene a owner
        mockMvc.perform(delete("/api/cityvoice/comments/" + commentId).with(user(otherUser.username())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("cancellare la storia di un altro utente è vietato")
    void deletingSomeoneElsesStoryIsForbidden() throws Exception {
        mockMvc.perform(delete("/api/cityvoice/stories/" + storyId).with(user(otherUser.username())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("cancellare una storia inesistente restituisce 404")
    void deletingMissingStoryReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/cityvoice/stories/" + UUID.randomUUID()).with(user(owner.username())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("senza autenticazione l'invio di una storia è respinto")
    void anonymousSubmissionIsRejected() throws Exception {
        // nessun .with(user(...)): richiesta anonima
        CreateStoryRequest request = new CreateStoryRequest(
                "decoro", "Trastevere", "Titolo", "Descrizione", "Contenuto");

        mockMvc.perform(post("/api/cityvoice/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}