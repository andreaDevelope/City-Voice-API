package it.cityvoice.api.features.impact;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactScoreServTest {

    private final ImpactScoreServ impactScoreServ = new ImpactScoreServ();

    @Test
    @DisplayName("un like su un contatore a zero vale +1")
    void likeOnZeroCounter() {
        assertEquals(1, impactScoreServ.computeAppliedDelta(0, ImpactScoreServ.LIKE_WEIGHT));
    }

    @Test
    @DisplayName("un dislike su un contatore positivo vale -1")
    void dislikeOnPositiveCounter() {
        assertEquals(-1, impactScoreServ.computeAppliedDelta(5, ImpactScoreServ.DISLIKE_WEIGHT));
    }

    @Test
    @DisplayName("un dislike su un contatore a zero viene assorbito dal floor")
    void dislikeOnZeroCounterIsAbsorbed() {
        assertEquals(0, impactScoreServ.computeAppliedDelta(0, ImpactScoreServ.DISLIKE_WEIGHT));
    }

    @Test
    @DisplayName("un commento su un contatore a zero vale +2")
    void commentOnZeroCounter() {
        assertEquals(2, impactScoreServ.computeAppliedDelta(0, ImpactScoreServ.COMMENT_WEIGHT));
    }

    @Test
    @DisplayName("un like vale sempre +1, qualunque sia il contatore")
    void likeAlwaysCountsFully() {
        assertEquals(1, impactScoreServ.computeAppliedDelta(0, ImpactScoreServ.LIKE_WEIGHT));
        assertEquals(1, impactScoreServ.computeAppliedDelta(1,ImpactScoreServ.LIKE_WEIGHT));
        assertEquals(1, impactScoreServ.computeAppliedDelta(100, ImpactScoreServ.LIKE_WEIGHT));
    }

    @Test
    @DisplayName("il delta non porta mai il contatore sotto zero")
    void neverGoesBelowZero() {
        int counter = 1;
        int delta = impactScoreServ.computeAppliedDelta(counter, ImpactScoreServ.DISLIKE_WEIGHT);
        assertEquals(0, counter + delta);

        counter = 0;
        delta = impactScoreServ.computeAppliedDelta(counter, ImpactScoreServ.DISLIKE_WEIGHT);
        assertEquals(0, counter + delta);
    }
}