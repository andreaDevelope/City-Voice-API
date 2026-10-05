package it.cityvoice.api.features.comments.controllers;

import it.cityvoice.api.features.comments.dto.CommentResponse;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.services.CommentServ;
import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.shared.retry.OptimisticRetryServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cityvoice/comments")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServ commentServ;
    private final UserRomeServ userRomeServ;
    private final OptimisticRetryServ optimisticRetryServ;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CreateCommentRequest request) {
        UserRome userRome = userRomeServ.findByUsername(user.getUsername());
        CommentResponse response = optimisticRetryServ.withRetry(() -> commentServ.createComment(userRome, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<List<CategoryProgressResponse>> deleteComment(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID commentId) {
        UserRome userRome = userRomeServ.findByUsername(user.getUsername());
        return ResponseEntity.ok(optimisticRetryServ.withRetry(() -> commentServ.deleteComment(userRome, commentId)));
    }
}