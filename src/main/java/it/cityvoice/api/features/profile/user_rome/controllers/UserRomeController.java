package it.cityvoice.api.features.profile.user_rome.controllers;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.services.AppUserService;
import it.cityvoice.api.features.profile.user_rome.dto.UpdateVisualIdentityRequest;
import it.cityvoice.api.features.profile.user_rome.dto.UserProfileResponse;
import it.cityvoice.api.features.profile.user_rome.dto.VisualIdentityResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserRomeController {
    private final UserRomeServ userRomeServ;
    private final AppUserService appUserService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails user) {
                AppUser appUser = appUserService.findByUsername(user.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
               UserRome userRome = userRomeServ.findByAppUserId(appUser.getId());
                UserProfileResponse response = new UserProfileResponse(
                                appUser.getUsername(),
                                userRome.getSymbol(),
                                userRome.getColor(),
                                userRome.getNeighborhood()
                                );
                return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/visual-identity")
    public ResponseEntity<VisualIdentityResponse> updateVisualIdentity(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody UpdateVisualIdentityRequest request) {
        AppUser appUser = appUserService.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        VisualIdentityResponse updated = userRomeServ.updateVisualIdentity(appUser.getId(), request);
        return ResponseEntity.ok(updated);
    }


}
