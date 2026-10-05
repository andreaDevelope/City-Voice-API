package it.cityvoice.api.features.auth;

import it.cityvoice.api.IntegrationTestBase;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class JwtRequestFilterIntegrationTest extends IntegrationTestBase {

    private static final Cookie MALFORMED_TOKEN = new Cookie("access_token", "non-un-jwt");

    @Test
    @DisplayName("un token malformato su un endpoint pubblico non blocca la richiesta")
    void malformedTokenOnPublicEndpointPassesAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/cityvoice/public/districts").cookie(MALFORMED_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("un token malformato su un endpoint protetto viene rifiutato, non va in errore")
    void malformedTokenOnProtectedEndpointIsRejected() throws Exception {
        mockMvc.perform(get("/api/cityvoice/badge/progress").cookie(MALFORMED_TOKEN))
                .andExpect(status().is4xxClientError());
    }
}