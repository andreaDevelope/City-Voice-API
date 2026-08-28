package it.cityvoice.api.features.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class RecoveryKeyService {
    private static final int WORD_COUNT = 6;
    private static final String SEPARATOR = "-";

    private final WordlistLoader wordlistLoader;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecoveryKeyService(WordlistLoader wordlistLoader, PasswordEncoder passwordEncoder) {
        this.wordlistLoader = wordlistLoader;
        this.passwordEncoder = passwordEncoder;
    }

    public record GeneratedKey(String plainKey, String hashedKey) {}

    public GeneratedKey generate() {
        var words = wordlistLoader.getWords();
        var selected = new StringBuilder();
        for (int i = 0; i < WORD_COUNT; i++) {
            if (i > 0) selected.append(SEPARATOR);
            int index = secureRandom.nextInt(words.size());
            selected.append(words.get(index).toLowerCase());
        }
        String plainKey = selected.toString();
        String hashedKey = passwordEncoder.encode(plainKey);
        return new GeneratedKey(plainKey, hashedKey);
    }

    public boolean matches(String plainKey, String hashedKey) {
        return passwordEncoder.matches(plainKey.toLowerCase().trim(), hashedKey);
    }
}
