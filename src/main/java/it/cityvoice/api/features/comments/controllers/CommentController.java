package it.cityvoice.api.features.comments.controllers;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.comments.dto.CommentResponse;
import it.cityvoice.api.features.comments.dto.CreateCommentRequest;
import it.cityvoice.api.features.comments.services.CommentServ;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cityvoice/comments")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServ commentServ;
    private final AppUserService appUserService;
    private final UserRomeServ userRomeServ;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CreateCommentRequest request) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());
        CommentResponse response = commentServ.createComment(userRome, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}