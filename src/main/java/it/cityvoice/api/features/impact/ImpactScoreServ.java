package it.cityvoice.api.features.impact;

import org.springframework.stereotype.Service;

@Service
public class ImpactScoreServ {

    public static final int LIKE_WEIGHT = 1;
    public static final int DISLIKE_WEIGHT = -1;
    public static final int COMMENT_WEIGHT = 2;

    public int computeAppliedDelta(int currentImpactCounter, int weight) {
        int visibleAfter = Math.max(0, currentImpactCounter + weight);
        return visibleAfter - currentImpactCounter;
    }
}