package it.cityvoice.api.features.stories.services;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.NeighborhoodScoreServ;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.reactions.entity.Reaction;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.dto.StoryResponse;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import it.cityvoice.api.shared.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Validated
@AllArgsConstructor
public class StoryServ {

    private static final String ACTIVITY_CATEGORY = "activity";
    private static final String NEIGHBORHOOD_CATEGORY = "neighborhood";

    private final StoryRepo storyRepo;
    private final UserRomeServ userRomeServ;
    private final BadgeServ badgeServ;
    private final CategoryServ categoryServ;
    private final UserBadgeService userBadgeService;
    private final UserBadgeRepo userBadgeRepo;
    private final NeighborhoodScoreServ neighborhoodScoreServ;
    private final CommentRepo commentRepo;
    private final ReactionRepo  reactionRepo;

    @Transactional
    public StoryResponse submitStory(UserRome userRome, @Valid CreateStoryRequest request) {
        Story story = new Story();
        story.setUserRome(userRome);
        story.setCategory(request.category());
        story.setDistrict(request.district());
        story.setTitle(request.title());
        story.setDescription(request.description());
        story.setStoryContent(request.storyContent());
        Story saved = storyRepo.save(story);

        userRome.setActivityCounter(userRome.getActivityCounter() + 1);
        unlockEligibleActivityBadges(userRome);

        updateNeighborhoodCounter(userRome);
        unlockEligibleNeighborhoodBadges(userRome);

        userRomeServ.save(userRome);

        List<CategoryProgressResponse> badgeProgress = categoryServ.getAllCategories().stream()
                .map(category -> badgeServ.getProgressForUser(userRome, category.getName()))
                .toList();

        return StoryResponse.from(saved, badgeProgress);
    }

    private void unlockEligibleActivityBadges(UserRome userRome) {
        Category category = categoryServ.getCategoryByName(ACTIVITY_CATEGORY);
        List<Badge> badges = badgeServ.getAllBadgesForCategory(category.getId());
        for (Badge badge : badges) {
            boolean thresholdReached = badge.getMissionThreshold() > 0
                    && badge.getMissionThreshold() <= userRome.getActivityCounter();
            if (thresholdReached && !userBadgeRepo.existsByUserRomeAndBadge(userRome, badge)) {
                userBadgeService.unlock(userRome, badge);
            }
        }
    }

    private void updateNeighborhoodCounter(UserRome userRome) {
        userRome.setNeighborhoodCounter(neighborhoodScoreServ.calculateDistinctDistrictCount(userRome));
    }

    private void unlockEligibleNeighborhoodBadges(UserRome userRome) {
        Category category = categoryServ.getCategoryByName(NEIGHBORHOOD_CATEGORY);
        List<Badge> badges = badgeServ.getAllBadgesForCategory(category.getId());
        for (Badge badge : badges) {
            boolean thresholdReached = badge.getMissionThreshold() > 0
                    && badge.getMissionThreshold() <= userRome.getNeighborhoodCounter();
            if (thresholdReached && !userBadgeRepo.existsByUserRomeAndBadge(userRome, badge)) {
                userBadgeService.unlock(userRome, badge);
            }
        }
    }

    @Transactional
    public List<CategoryProgressResponse> deleteStory(UserRome requester, UUID storyId) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Storia non trovata"));

        if (!Objects.equals(requester.getId(), story.getUserRome().getId())) {
            throw new UnauthorizedException("Non puoi eliminare una storia che non è tua");
        }

        int lostImpact = 0;

        for (Reaction reaction : reactionRepo.findByStory(story)) {
            lostImpact += reaction.getAppliedDelta();
        }

        for (Comment comment : commentRepo.findByStory(story)) {
            lostImpact += comment.getStoryBonusDelta();
            if (comment.getParentComment() == null) {
                lostImpact += comment.getAppliedDelta();
            }
            reactionRepo.deleteAll(reactionRepo.findByComment(comment));
        }

        commentRepo.deleteAll(commentRepo.findByStory(story));
        reactionRepo.deleteAll(reactionRepo.findByStory(story));
        storyRepo.delete(story);

        requester.setImpactCounter(Math.max(0, requester.getImpactCounter() - lostImpact));
        requester.setActivityCounter(Math.max(0, requester.getActivityCounter() - 1));
        requester.setNeighborhoodCounter(neighborhoodScoreServ.calculateDistinctDistrictCount(requester));
        userRomeServ.save(requester);

        return categoryServ.getAllCategories().stream()
                .map(category -> badgeServ.getProgressForUser(requester, category.getName()))
                .toList();
    }
}