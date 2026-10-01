package it.cityvoice.api.features.profile.user_rome.services;

import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.districts.enums.Municipio;
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

    public int calculateDistinctMunicipioCount(UserRome userRome) {
        Set<Municipio> municipi = new HashSet<>(storyRepo.findDistinctMunicipi(userRome, StoryStatus.BLOCKED));
        municipi.addAll(commentRepo.findDistinctCommentedMunicipi(userRome, StoryStatus.BLOCKED));
        return municipi.size();
    }
}