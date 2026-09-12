package it.cityvoice.api.features.reactions.controllers;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.features.reactions.dto.ReactToContentRequest;
import it.cityvoice.api.features.reactions.dto.ReactionResponse;
import it.cityvoice.api.features.reactions.services.ReactionServ;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cityvoice/reactions")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionServ reactionServ;
    private final AppUserService appUserService;
    private final UserRomeServ userRomeServ;

    @PostMapping
    public ResponseEntity<ReactionResponse> react(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ReactToContentRequest request) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());
        ReactionResponse response = reactionServ.react(userRome, request);
        return ResponseEntity.ok(response);
    }
}