package it.cityvoice.api.features.comments.repositories;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {

    long countByStoryAndParentCommentIsNull(Story story);

    long countByParentComment(Comment parentComment);

    @Query("SELECT DISTINCT c.story.district FROM Comment c " +
            "WHERE c.userRome = :userRome AND c.story.status <> :excludedStatus")
    List<String> findDistinctCommentedDistricts(@Param("userRome") UserRome userRome,
                                                @Param("excludedStatus") StoryStatus excludedStatus);
}