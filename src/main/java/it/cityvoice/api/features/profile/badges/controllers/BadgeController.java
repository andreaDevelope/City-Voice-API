package it.cityvoice.api.features.profile.badges.controllers;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cityvoice/badge")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BadgeController {
    private final BadgeServ badgeServ;
    private final AppUserService appUserService;
    private final UserRomeServ  userRomeServ;
    private final CategoryServ categoryServ;

    @GetMapping("/progress")
    public ResponseEntity<List<CategoryProgressResponse>> getMyProgress(@AuthenticationPrincipal UserDetails user) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());

        List<CategoryProgressResponse> progress = categoryServ.getAllCategories().stream()
                .map(category -> badgeServ.getProgressForUser(userRome, category.getName()))
                .toList();

        return ResponseEntity.ok(progress);
    }
}
