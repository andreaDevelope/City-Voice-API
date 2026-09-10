package it.cityvoice.api.features.stories.repositories;

import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoryRepo extends JpaRepository<Story, UUID> {
    Long countDistinctDistrictByUserRomeAndStatusNot(UserRome userRome, StoryStatus excludedStatus);
}
