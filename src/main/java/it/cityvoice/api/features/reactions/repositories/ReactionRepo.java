package it.cityvoice.api.features.reactions.repositories;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.reactions.entity.Reaction;
import it.cityvoice.api.features.stories.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepo extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserRomeAndStory(UserRome userRome, Story story);

    Optional<Reaction> findByUserRomeAndComment(UserRome userRome, Comment comment);

    List<Reaction> findByStory(Story story);

    List<Reaction> findByComment(Comment comment);
}