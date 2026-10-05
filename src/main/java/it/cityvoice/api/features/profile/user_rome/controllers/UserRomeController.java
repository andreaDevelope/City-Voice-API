package it.cityvoice.api.features.profile.user_rome.controllers;

import it.cityvoice.api.features.profile.user_rome.dto.UpdateVisualIdentityRequest;
import it.cityvoice.api.features.profile.user_rome.dto.UserProfileResponse;
import it.cityvoice.api.features.profile.user_rome.dto.VisualIdentityResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cityvoice/profile")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserRomeController {
    private final UserRomeServ userRomeServ;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails user) {
                UserRome userRome = userRomeServ.findByUsername(user.getUsername());
                UserProfileResponse response = new UserProfileResponse(
                                user.getUsername(),
                                userRome.getSymbol(),
                                userRome.getColor(),
                                userRome.getNeighborhood()
                                );
                return ResponseEntity.ok(response);
    }

    @PutMapping("/visual-identity")
    public ResponseEntity<VisualIdentityResponse> updateVisualIdentity(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody UpdateVisualIdentityRequest request) {
        UserRome userRome = userRomeServ.findByUsername(user.getUsername());
        VisualIdentityResponse updated = userRomeServ.updateVisualIdentity(userRome, request);
        return ResponseEntity.ok(updated);
    }


}
