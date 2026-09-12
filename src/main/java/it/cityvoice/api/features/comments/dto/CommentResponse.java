package it.cityvoice.api.features.comments.dto;

import it.cityvoice.api.features.comments.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID storyId,
        UUID parentCommentId,
        String content,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getStory().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}