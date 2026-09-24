package it.cityvoice.api.features.stories.controllers;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.stories.dto.CreateStoryRequest;
import it.cityvoice.api.features.stories.dto.StoryResponse;
import it.cityvoice.api.features.stories.services.StoryServ;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import it.cityvoice.api.shared.retry.OptimisticRetryServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cityvoice/stories")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class StoryController {

    private final StoryServ storyServ;
    private final AppUserService appUserService;
    private final UserRomeServ userRomeServ;
    private final OptimisticRetryServ optimisticRetryServ;

    @PostMapping
    public ResponseEntity<StoryResponse> submitStory(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CreateStoryRequest request) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());
        StoryResponse response = optimisticRetryServ.withRetry(() -> storyServ.submitStory(userRome, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<List<CategoryProgressResponse>> deleteStory(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID storyId) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());
        return ResponseEntity.ok(optimisticRetryServ.withRetry(() -> storyServ.deleteStory(userRome, storyId)));
    }
}