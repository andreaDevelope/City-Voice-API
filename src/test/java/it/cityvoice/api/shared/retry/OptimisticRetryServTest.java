package it.cityvoice.api.shared.retry;

import it.cityvoice.api.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptimisticRetryServTest extends IntegrationTestBase {

    @Autowired
    private OptimisticRetryServ optimisticRetryServ;

    @Test
    @DisplayName("la lamba passa senza conflitti")
    void noRetry(){
        AtomicInteger counter = new AtomicInteger(0);
        optimisticRetryServ.withRetry(()->{
            counter.incrementAndGet();
            return "ok";
        });
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("un conflitto isolato viene superato al secondo tentativo")
    void transientConflictIsRetried() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = optimisticRetryServ.withRetry(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new OptimisticLockingFailureException("conflitto simulato");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, attempts.get());
    }

    @Test
    @DisplayName("dopo tre tentativi falliti l'eccezione viene propagata")
    void exhaustedRetriesPropagate() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThrows(OptimisticLockingFailureException.class, () ->
                optimisticRetryServ.withRetry(() -> {
                    attempts.incrementAndGet();
                    throw new OptimisticLockingFailureException("conflitto permanente");
                }));

        assertEquals(3, attempts.get());
    }

    @Test
    @DisplayName("esce al lancio di un altra eccezione")
    void noRetryIfStrangeException() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThrows(IllegalStateException.class, () ->
                optimisticRetryServ.withRetry(() -> {
                    attempts.incrementAndGet();
                    throw new IllegalStateException("errore stato");
                }));

        assertEquals(1, attempts.get());
    }
}