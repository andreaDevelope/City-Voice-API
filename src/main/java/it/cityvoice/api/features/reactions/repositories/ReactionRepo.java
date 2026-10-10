package it.cityvoice.api.features.reactions.repositories;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.reactions.entity.Reaction;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.stories.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepo extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserRomeAndStory(UserRome userRome, Story story);

    Optional<Reaction> findByUserRomeAndComment(UserRome userRome, Comment comment);

    List<Reaction> findByStory(Story story);

    List<Reaction> findByComment(Comment comment);

    // conteggio per storia e tipo, solo reazioni su storie (non su commenti)
    @Query("SELECT r.story.id AS storyId, r.type AS type, COUNT(r) AS count " +
            "FROM Reaction r WHERE r.story.id IN :storyIds GROUP BY r.story.id, r.type")
    List<ReactionCountProjection> countByStoryIdAndType(@Param("storyIds") Collection<UUID> storyIds);

    interface ReactionCountProjection {
        UUID getStoryId();
        ReactionType getType();
        long getCount();
    }
}