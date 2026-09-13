package it.cityvoice.api;

import it.cityvoice.api.features.auth.dto.RegisterRequest;
import it.cityvoice.api.features.auth.enums.Role;
import it.cityvoice.api.features.auth.services.AppUserService;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    protected static final Faker FAKER = new Faker(Locale.ITALY);

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AppUserService appUserService;

    // registra un utente con username casuale, restituendo id e username
    protected TestUser registerUser() {
        String name = FAKER.name().firstName().replaceAll("[^a-zA-Z]", "");
        String username = name.substring(0, Math.min(name.length(), 8)) + FAKER.number().numberBetween(100, 999);
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password1");
        Long appUserId = appUserService.registerUser(request, Set.of(Role.ROLE_USER)).user().getId();
        return new TestUser(appUserId, username);
    }

    protected record TestUser(Long appUserId, String username) {}
}