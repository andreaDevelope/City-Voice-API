package it.cityvoice.api.features.profile.user_badge.controllers;

import it.cityvoice.api.features.profile.user_badge.dto.FeaturedBadgesRequest;
import it.cityvoice.api.features.profile.user_badge.dto.UnlockedBadgeResponse;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cityvoice/badge")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;
    private final UserRomeServ userRomeServ;

    @GetMapping("/unlocked")
    public ResponseEntity<List<UnlockedBadgeResponse>> getUnlockedBadges(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(userBadgeService.getUnlockedBadges(userRomeServ.findByUsername(user.getUsername())));
    }

    @PutMapping("/featured")
    public ResponseEntity<List<UnlockedBadgeResponse>> setFeaturedBadges(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody FeaturedBadgesRequest request) {
        return ResponseEntity.ok(userBadgeService.setFeaturedBadges(userRomeServ.findByUsername(user.getUsername()), request));
    }
}