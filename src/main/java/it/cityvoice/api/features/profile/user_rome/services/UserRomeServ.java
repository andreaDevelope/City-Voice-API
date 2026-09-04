package it.cityvoice.api.features.profile.user_rome.services;

import it.cityvoice.api.features.profile.user_rome.dto.UpdateVisualIdentityRequest;
import it.cityvoice.api.features.profile.user_rome.dto.VisualIdentityResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserRomeServ {
    private final UserRomeRepo userRepo;

    public UserRome findByAppUserId(Long id) {
        return userRepo.findByAppUserId(id);
    }

    public UserRome save(UserRome userRome) {
        return userRepo.save(userRome);
    }

    public VisualIdentityResponse updateVisualIdentity(Long appUserId, @Valid UpdateVisualIdentityRequest request) {
         UserRome userRome = userRepo.findByAppUserId(appUserId);
         userRome.setSymbol(request.symbol());
         userRome.setColor(request.color());
         UserRome saved = userRepo.save(userRome);
         return new VisualIdentityResponse(saved.getSymbol(), saved.getColor());
    }


}
