package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.user_rome.dto.UpdateNeighborhoodRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    @DisplayName("un utente appena registrato non ha un quartiere")
    void freshUserHasNoNeighborhood() throws Exception {
        TestUser owner = registerUser();

        mockMvc.perform(get("/api/cityvoice/profile/me")
                        .with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood").value((Object) null));
    }

    @Test
    @DisplayName("impostare il quartiere lo restituisce nel profilo")
    void settingNeighborhoodReturnsItInProfile() throws Exception {
        TestUser owner = registerUser();

        mockMvc.perform(put("/api/cityvoice/profile/neighborhood")
                        .with(user(owner.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNeighborhoodRequest(13L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood.id").value(13))
                .andExpect(jsonPath("$.neighborhood.name").value("Trastevere"))
                .andExpect(jsonPath("$.neighborhood.municipio").value("I"))
                .andExpect(jsonPath("$.neighborhood.municipioLabel").value("Centro Storico"));

        mockMvc.perform(get("/api/cityvoice/profile/me")
                        .with(user(owner.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood.id").value(13))
                .andExpect(jsonPath("$.neighborhood.name").value("Trastevere"))
                .andExpect(jsonPath("$.neighborhood.municipio").value("I"))
                .andExpect(jsonPath("$.neighborhood.municipioLabel").value("Centro Storico"));
    }

    @Test
    @DisplayName("passare un districtId nullo rimuove il quartiere")
    void clearingNeighborhoodWithNullDistrictId() throws Exception {
        TestUser owner = registerUser();

        mockMvc.perform(put("/api/cityvoice/profile/neighborhood")
                        .with(user(owner.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNeighborhoodRequest(13L))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/cityvoice/profile/neighborhood")
                        .with(user(owner.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNeighborhoodRequest(null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood").value((Object) null));
    }

    @Test
    @DisplayName("un quartiere inesistente restituisce 404")
    void unknownDistrictIdReturnsNotFound() throws Exception {
        TestUser owner = registerUser();

        mockMvc.perform(put("/api/cityvoice/profile/neighborhood")
                        .with(user(owner.username()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNeighborhoodRequest(999999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Quartiere non trovato"));
    }
}
