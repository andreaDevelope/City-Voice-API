package it.cityvoice.api.features.impact;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
import it.cityvoice.api.features.stories.entity.Story;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImpactScoreServ {

    public static final int LIKE_WEIGHT = 1;
    public static final int DISLIKE_WEIGHT = -1;
    public static final int COMMENT_WEIGHT = 2;

    private final ReactionRepo reactionRepo;
    private final CommentRepo commentRepo;

    public int getRawScoreForStory(Story story) {
        long likes = reactionRepo.countByStoryAndType(story, ReactionType.LIKE);
        long dislikes = reactionRepo.countByStoryAndType(story, ReactionType.DISLIKE);
        long comments = commentRepo.countByStoryAndParentCommentIsNull(story);
        return (int) (likes * LIKE_WEIGHT + dislikes * DISLIKE_WEIGHT + comments * COMMENT_WEIGHT);
    }

    public int getRawScoreForComment(Comment comment) {
        long likes = reactionRepo.countByCommentAndType(comment, ReactionType.LIKE);
        long dislikes = reactionRepo.countByCommentAndType(comment, ReactionType.DISLIKE);
        long replies = commentRepo.countByParentComment(comment);
        return (int) (likes * LIKE_WEIGHT + dislikes * DISLIKE_WEIGHT + replies * COMMENT_WEIGHT);
    }

    public int computeAppliedDelta(int currentImpactCounter, int weight) {
        int visibleAfter = Math.max(0, currentImpactCounter + weight);
        return visibleAfter - currentImpactCounter;
    }
}
