package it.cityvoice.api.features.reactions.services;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.impact.ImpactScoreServ;
import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.dto.ReactionResponse;
import it.cityvoice.api.features.reactions.entity.Reaction;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
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
public class ReactionServ {

    private static final String IMPACT_CATEGORY = "impact";

    private final ReactionRepo reactionRepo;
    private final StoryRepo storyRepo;
    private final CommentRepo commentRepo;
    private final ImpactScoreServ impactScoreServ;
    private final UserRomeServ userRomeServ;
    private final BadgeServ badgeServ;
    private final CategoryServ categoryServ;
    private final UserBadgeService userBadgeService;
    private final UserBadgeRepo userBadgeRepo;

    @Transactional
    public ReactionResponse react(UserRome reactingUser, @Valid ReactToContentRequest request) {
        validateExactlyOneTarget(request);

        Story story = request.storyId() != null
                ? storyRepo.findById(request.storyId()).orElseThrow(() -> new ResourceNotFoundException("Storia non trovata"))
                : null;
        Comment comment = request.commentId() != null
                ? commentRepo.findById(request.commentId()).orElseThrow(() -> new ResourceNotFoundException("Commento non trovato"))
                : null;

        UserRome targetOwner = story != null ? story.getUserRome() : comment.getUserRome();

        var existing = story != null
                ? reactionRepo.findByUserRomeAndStory(reactingUser, story)
                : reactionRepo.findByUserRomeAndComment(reactingUser, comment);

        if (existing.isPresent() && existing.get().getType() == request.type()) {
            removeReaction(existing.get(), targetOwner);
        } else if (existing.isPresent()) {
            replaceReaction(existing.get(), request.type(), targetOwner);
        } else {
            createReaction(reactingUser, request.type(), story, comment, targetOwner);
        }

        List<CategoryProgressResponse> badgeProgress = categoryServ.getAllCategories().stream()
                .map(category -> badgeServ.getProgressForUser(reactingUser, category.getName()))
                .toList();
        return new ReactionResponse(badgeProgress);
    }

    private void createReaction(UserRome reactingUser, ReactionType type, Story story, Comment comment, UserRome targetOwner) {
        int appliedDelta = isSameUser(reactingUser, targetOwner)
                ? 0
                : impactScoreServ.computeAppliedDelta(targetOwner.getImpactCounter(), weightFor(type));

        Reaction reaction = new Reaction();
        reaction.setUserRome(reactingUser);
        reaction.setStory(story);
        reaction.setComment(comment);
        reaction.setType(type);
        reaction.setAppliedDelta(appliedDelta);
        reactionRepo.save(reaction);

        if (appliedDelta != 0) {
            applyImpactDelta(targetOwner, appliedDelta);
        }
    }

    private void removeReaction(Reaction reaction, UserRome targetOwner) {
        if (reaction.getAppliedDelta() != 0) {
            applyImpactDelta(targetOwner, -reaction.getAppliedDelta());
        }
        reactionRepo.delete(reaction);
    }

    private void replaceReaction(Reaction reaction, ReactionType newType, UserRome targetOwner) {
        if (reaction.getAppliedDelta() != 0) {
            applyImpactDelta(targetOwner, -reaction.getAppliedDelta());
        }

        int appliedDelta = isSameUser(reaction.getUserRome(), targetOwner)
                ? 0
                : impactScoreServ.computeAppliedDelta(targetOwner.getImpactCounter(), weightFor(newType));

        reaction.setType(newType);
        reaction.setAppliedDelta(appliedDelta);
        reactionRepo.save(reaction);

        if (appliedDelta != 0) {
            applyImpactDelta(targetOwner, appliedDelta);
        }
    }

    private void applyImpactDelta(UserRome targetOwner, int delta) {
        targetOwner.setImpactCounter(targetOwner.getImpactCounter() + delta);
        unlockEligibleImpactBadges(targetOwner);
        userRomeServ.save(targetOwner);
    }

    private int weightFor(ReactionType type) {
        return type == ReactionType.LIKE ? ImpactScoreServ.LIKE_WEIGHT : ImpactScoreServ.DISLIKE_WEIGHT;
    }

    private boolean isSameUser(UserRome first, UserRome second) {
        return Objects.equals(first.getId(), second.getId());
    }

    private void validateExactlyOneTarget(ReactToContentRequest request) {
        boolean hasStory = request.storyId() != null;
        boolean hasComment = request.commentId() != null;
        if (hasStory == hasComment) {
            throw new BadRequestException("Devi specificare esattamente una storia o un commento, non entrambi o nessuno");
        }
    }

    private void unlockEligibleImpactBadges(UserRome userRome) {
        Category category = categoryServ.getCategoryByName(IMPACT_CATEGORY);
        List<Badge> badges = badgeServ.getAllBadgesForCategory(category.getId());
        for (Badge badge : badges) {
            boolean thresholdReached = badge.getMissionThreshold() > 0 && badge.getMissionThreshold() <= userRome.getImpactCounter();
            if (thresholdReached && !userBadgeRepo.existsByUserRomeAndBadge(userRome, badge)) {
                userBadgeService.unlock(userRome, badge);
            }
        }
    }
}