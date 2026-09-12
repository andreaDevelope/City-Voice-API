package it.cityvoice.api.features.comments.services;

import it.cityvoice.api.features.comments.dto.CommentResponse;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.comments.repositories.CommentRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.repositories.StoryRepo;
import it.cityvoice.api.shared.exceptions.BadRequestException;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class CommentServ {

    private final CommentRepo commentRepo;
    private final StoryRepo storyRepo;

    @Transactional
    public CommentResponse createComment(UserRome userRome, @Valid CreateCommentRequest request) {
        Story story = storyRepo.findById(request.storyId())
                .orElseThrow(() -> new ResourceNotFoundException("Storia non trovata"));

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            parentComment = commentRepo.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commento padre non trovato"));
            if (!parentComment.getStory().getId().equals(story.getId())) {
                throw new BadRequestException("Il commento padre non appartiene a questa storia");
            }
        }

        Comment comment = new Comment();
        comment.setUserRome(userRome);
        comment.setStory(story);
        comment.setParentComment(parentComment);
        comment.setContent(request.content());

        Comment saved = commentRepo.save(comment);
        return CommentResponse.from(saved);
    }
}