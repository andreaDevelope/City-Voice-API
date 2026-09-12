package it.cityvoice.api.features.comments.services;

import it.cityvoice.api.features.comments.dto.CommentResponse;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.impact.ImpactScoreServ;
import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.NeighborhoodScoreServ;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import it.cityvoice.api.shared.exceptions.BadRequestException;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
@RequiredArgsConstructor
public class CommentServ {

    private static final String ACTIVITY_CATEGORY = "activity";
    private static final String IMPACT_CATEGORY = "impact";
    private static final String NEIGHBORHOOD_CATEGORY = "neighborhood";
    private static final int COMMENT_ACTIVITY_POINTS = 1;
    private static final int STORY_BONUS_POINTS = 1;
    private static final int MAX_BONUS_DEPTH = 2;

    private final CommentRepo commentRepo;
    private final StoryRepo storyRepo;
    private final UserRomeServ userRomeServ;
    private final BadgeServ badgeServ;
    private final CategoryServ categoryServ;
    private final UserBadgeService userBadgeService;
    private final UserBadgeRepo userBadgeRepo;
    private final ImpactScoreServ impactScoreServ;
    private final NeighborhoodScoreServ neighborhoodScoreServ;

    @Transactional
    public CommentResponse createComment(UserRome author, @Valid CreateCommentRequest request) {
        Story story = storyRepo.findById(request.storyId())
                .orElseThrow(() -> new ResourceNotFoundException("Storia non trovata"));

        Comment parentComment = resolveParentComment(request, story);

        UserRome targetOwner = parentComment != null ? parentComment.getUserRome() : story.getUserRome();
        UserRome storyOwner = story.getUserRome();
        int depth = parentComment != null ? parentComment.getDepth() + 1 : 0;

        Comment comment = new Comment();
        comment.setUserRome(author);
        comment.setStory(story);
        comment.setParentComment(parentComment);
        comment.setContent(request.content());
        comment.setDepth(depth);
        Comment saved = commentRepo.save(comment);

        int appliedDelta = isSameUser(author, targetOwner)
                ? 0
                : impactScoreServ.computeAppliedDelta(targetOwner.getImpactCounter(), ImpactScoreServ.COMMENT_WEIGHT);
        if (appliedDelta != 0) {
            awardImpact(targetOwner, appliedDelta);
        }

        int storyBonusDelta = 0;
        if (isStoryBonusEligible(author, targetOwner, storyOwner, depth)) {
            storyBonusDelta = impactScoreServ.computeAppliedDelta(storyOwner.getImpactCounter(), STORY_BONUS_POINTS);
            if (storyBonusDelta != 0) {
                awardImpact(storyOwner, storyBonusDelta);
            }
        }

        saved.setAppliedDelta(appliedDelta);
        saved.setStoryBonusDelta(storyBonusDelta);
        saved = commentRepo.save(saved);

        author.setActivityCounter(author.getActivityCounter() + COMMENT_ACTIVITY_POINTS);
        unlockEligibleBadges(author, ACTIVITY_CATEGORY, author.getActivityCounter());

        author.setNeighborhoodCounter(neighborhoodScoreServ.calculateDistinctDistrictCount(author));
        unlockEligibleBadges(author, NEIGHBORHOOD_CATEGORY, author.getNeighborhoodCounter());

        userRomeServ.save(author);

        return CommentResponse.from(saved);
    }

    private Comment resolveParentComment(CreateCommentRequest request, Story story) {
        if (request.parentCommentId() == null) {
            return null;
        }
        Comment parentComment = commentRepo.findById(request.parentCommentId())
                .orElseThrow(() -> new ResourceNotFoundException("Commento padre non trovato"));
        if (!parentComment.getStory().getId().equals(story.getId())) {
            throw new BadRequestException("Il commento padre non appartiene a questa storia");
        }
        return parentComment;
    }

    private boolean isStoryBonusEligible(UserRome author, UserRome targetOwner, UserRome storyOwner, int depth) {
        return depth >= 1
                && depth <= MAX_BONUS_DEPTH
                && !isSameUser(storyOwner, targetOwner)
                && !isSameUser(storyOwner, author);
    }

    private boolean isSameUser(UserRome first, UserRome second) {
        return Objects.equals(first.getId(), second.getId());
    }

    private void awardImpact(UserRome beneficiary, int delta) {
        beneficiary.setImpactCounter(beneficiary.getImpactCounter() + delta);
        unlockEligibleBadges(beneficiary, IMPACT_CATEGORY, beneficiary.getImpactCounter());
        userRomeServ.save(beneficiary);
    }

    private void unlockEligibleBadges(UserRome userRome, String categoryName, int counter) {
        Category category = categoryServ.getCategoryByName(categoryName);
        List<Badge> badges = badgeServ.getAllBadgesForCategory(category.getId());
        for (Badge badge : badges) {
            boolean thresholdReached = badge.getMissionThreshold() > 0 && badge.getMissionThreshold() <= counter;
            if (thresholdReached && !userBadgeRepo.existsByUserRomeAndBadge(userRome, badge)) {
                userBadgeService.unlock(userRome, badge);
            }
        }
    }
}