package it.cityvoice.api.features.auth.service;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecoveryAttemptLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_SECONDS = 300; // 5 minuti

    private record AttemptRecord(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String username) {
        var record = attempts.get(username);
        if (record == null) return false;
        if (record.lockedUntil() != null && Instant.now().isBefore(record.lockedUntil())) {
            return true;
        }
        if (record.lockedUntil() != null) {
            attempts.remove(username);
        }
        return false;
    }

    public void recordFailure(String username) {
        attempts.compute(username, (k, v) -> {
            int newCount = (v == null ? 0 : v.count()) + 1;
            Instant lockUntil = newCount >= MAX_ATTEMPTS
                ? Instant.now().plusSeconds(LOCKOUT_SECONDS)
                : null;
            return new AttemptRecord(newCount, lockUntil);
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }
}
