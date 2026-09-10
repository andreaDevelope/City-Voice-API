package it.cityvoice.api.features.stories.services;

import it.cityvoice.api.features.profile.badge.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.badge.entity.Badge;
import it.cityvoice.api.features.profile.badge.services.BadgeServ;
import it.cityvoice.api.features.profile.category.entity.Category;
import it.cityvoice.api.features.profile.category.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.dto.StoryResponse;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
        Long distinctDistricts = storyRepo.countDistinctDistrictByUserRomeAndStatusNot(userRome, StoryStatus.BLOCKED);
        userRome.setNeighborhoodCounter(distinctDistricts.intValue());
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
}