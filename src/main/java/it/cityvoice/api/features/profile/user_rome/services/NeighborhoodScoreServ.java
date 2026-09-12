package it.cityvoice.api.features.profile.user_rome.services;

import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NeighborhoodScoreServ {

    private final StoryRepo storyRepo;
    private final CommentRepo commentRepo;

    public int calculateDistinctDistrictCount(UserRome userRome) {
        Set<String> districts = new HashSet<>(storyRepo.findDistinctDistricts(userRome, StoryStatus.BLOCKED));
        districts.addAll(commentRepo.findDistinctCommentedDistricts(userRome, StoryStatus.BLOCKED));
        return districts.size();
    }
}