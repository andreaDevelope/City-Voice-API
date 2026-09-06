package it.cityvoice.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Consente tutte le richieste su tutti gli endpoint
                        .allowedOrigins("http://localhost:4200", "http://192.168.20.140:4200")
                        .allowedMethods("*") // Consente tutti i metodi (GET, POST, PUT, DELETE, PATCH, OPTIONS)
                        .allowedHeaders("*") // Consente tutti gli header
                        .allowCredentials(true); // Consente l'invio di credenziali, mettere false se allowedOrigins è "*"
            }
        };
    }
}
