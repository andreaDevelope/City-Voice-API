package it.cityvoice.api.shared.retry;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

// Riesegue un'operazione quando due transazioni aggiornano lo stesso UserRome in parallelo.
// Dettagli: docs/02-scoring-and-badges.md#concurrent-counter-updates
@Service
public class OptimisticRetryServ {

    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public <T> T withRetry(Supplier<T> action) {
        return action.get();
    }
}