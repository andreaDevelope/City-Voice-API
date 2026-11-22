package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ProfileIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("il profilo autenticato restituisce username e data di iscrizione")
    void getMyProfileReturnsUsernameAndMemberSince() throws Exception {
        TestUser owner = registerUser();

        String body = mockMvc.perform(get("/api/cityvoice/profile/me")
                        .with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(owner.username()))
                .andExpect(jsonPath("$.memberSince").exists())
                .andReturn().getResponse().getContentAsString();

        Instant memberSince = Instant.parse(objectMapper.readTree(body).get("memberSince").asString());
        assertTrue(Duration.between(memberSince, Instant.now()).toMinutes() < 5);
    }
}
