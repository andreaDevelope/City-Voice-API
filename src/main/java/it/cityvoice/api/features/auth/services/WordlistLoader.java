package it.cityvoice.api.features.auth.services;

import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class WordlistLoader {
    private List<String> words;

    @PostConstruct
    public void load() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("wordlist-it.txt").getInputStream(),
                StandardCharsets.UTF_8))) {
            words = reader.lines().filter(line -> !line.isBlank()).toList();
            if (words.isEmpty()) {
                throw new IllegalStateException("wordlist-it.txt è vuota");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare wordlist-it.txt", e);
        }
    }

    public List<String> getWords() {
        return words;
    }

    public int size() {
        return words.size();
    }
}
