package it.cityvoice.api.features.stats.services;

import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.stats.dto.PublicStatsResponse;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lives in its own feature because the counts span users and stories, so it belongs to neither domain
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicStatsServ {

    private final UserRomeRepo userRomeRepo;
    private final StoryRepo storyRepo;

    public PublicStatsResponse getStats() {
        return new PublicStatsResponse(
                userRomeRepo.count(),
                storyRepo.countByStatusAndType(StoryStatus.PUBLISHED, StoryType.STORY),
                storyRepo.countByStatusAndType(StoryStatus.PUBLISHED, StoryType.REPORT)
        );
    }
}
