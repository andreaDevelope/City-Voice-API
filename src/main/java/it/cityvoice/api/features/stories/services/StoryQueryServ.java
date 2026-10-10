package it.cityvoice.api.features.stories.services;

import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.comments.repositories.CommentRepo.CommentCountProjection;
import it.cityvoice.api.features.districts.enums.Municipio;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo;
import it.cityvoice.api.features.reactions.repositories.ReactionRepo.ReactionCountProjection;
import it.cityvoice.api.features.stories.dto.FeaturedBadgeResponse;
import it.cityvoice.api.features.stories.dto.StoryAuthorResponse;
import it.cityvoice.api.features.stories.dto.StoryCardResponse;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import it.cityvoice.api.features.stories.specifications.StorySpecifications;
import it.cityvoice.api.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryQueryServ {

    private static final int DEFAULT_MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    private final StoryRepo storyRepo;
    private final ReactionRepo reactionRepo;
    private final CommentRepo commentRepo;
    private final UserBadgeRepo userBadgeRepo;

    public PageResponse<StoryCardResponse> search(String q, String category, int page, int size) {
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = Math.min(Math.max(size, DEFAULT_MIN_SIZE), MAX_SIZE);

        String trimmedQuery = q == null ? null : q.trim();
        String trimmedCategory = category == null ? null : category.trim();

        Specification<Story> spec = StorySpecifications.published();
        if (trimmedCategory != null && !trimmedCategory.isEmpty()) {
            spec = spec.and(StorySpecifications.ofCategory(trimmedCategory));
        }
        if (trimmedQuery != null && !trimmedQuery.isEmpty()) {
            spec = spec.and(StorySpecifications.matches(trimmedQuery, matchingMunicipi(trimmedQuery)));
        }

        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> result = storyRepo.findAll(spec, pageable);

        List<Story> stories = result.getContent();
        if (stories.isEmpty()) {
            return new PageResponse<>(List.of(), resolvedPage, resolvedSize, result.hasNext());
        }

        List<UUID> storyIds = stories.stream().map(Story::getId).toList();
        List<Long> authorIds = stories.stream().map(story -> story.getUserRome().getId()).distinct().toList();

        Map<UUID, Map<ReactionType, Long>> reactionCounts = groupReactionCounts(reactionRepo.countByStoryIdAndType(storyIds));
        Map<UUID, Long> commentCounts = commentRepo.countByStoryIdIn(storyIds).stream()
                .collect(Collectors.toMap(CommentCountProjection::getStoryId, CommentCountProjection::getCount));
        Map<Long, List<FeaturedBadgeResponse>> featuredBadges = groupFeaturedBadges(userBadgeRepo.findFeaturedByUserRomeIdIn(authorIds));

        List<StoryCardResponse> items = stories.stream()
                .map(story -> toCard(story, reactionCounts, commentCounts, featuredBadges))
                .toList();

        return new PageResponse<>(items, resolvedPage, resolvedSize, result.hasNext());
    }

    private StoryCardResponse toCard(Story story,
                                      Map<UUID, Map<ReactionType, Long>> reactionCounts,
                                      Map<UUID, Long> commentCounts,
                                      Map<Long, List<FeaturedBadgeResponse>> featuredBadges) {
        Map<ReactionType, Long> counts = reactionCounts.getOrDefault(story.getId(), Map.of());
        long likes = counts.getOrDefault(ReactionType.LIKE, 0L);
        long dislikes = counts.getOrDefault(ReactionType.DISLIKE, 0L);
        long comments = commentCounts.getOrDefault(story.getId(), 0L);

        Long authorId = story.getUserRome().getId();
        StoryAuthorResponse author = StoryAuthorResponse.from(
                story.getUserRome(), featuredBadges.getOrDefault(authorId, List.of()));

        return StoryCardResponse.from(story, author, likes, dislikes, comments);
    }

    private List<Municipio> matchingMunicipi(String query) {
        String lower = query.toLowerCase();
        return Arrays.stream(Municipio.values())
                .filter(m -> m.getLabel().toLowerCase().contains(lower) || m.name().equalsIgnoreCase(query))
                .toList();
    }

    private Map<UUID, Map<ReactionType, Long>> groupReactionCounts(List<ReactionCountProjection> rows) {
        Map<UUID, Map<ReactionType, Long>> result = new HashMap<>();
        for (ReactionCountProjection row : rows) {
            result.computeIfAbsent(row.getStoryId(), id -> new HashMap<>()).put(row.getType(), row.getCount());
        }
        return result;
    }

    private Map<Long, List<FeaturedBadgeResponse>> groupFeaturedBadges(List<UserBadge> userBadges) {
        Map<Long, List<FeaturedBadgeResponse>> result = new LinkedHashMap<>();
        for (UserBadge userBadge : userBadges) {
            result.computeIfAbsent(userBadge.getUserRome().getId(), id -> new ArrayList<>())
                    .add(FeaturedBadgeResponse.from(userBadge));
        }
        return result;
    }
}
