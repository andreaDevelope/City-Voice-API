package it.cityvoice.api.features.profile.user_badge.services;

import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.user_badge.dto.FeaturedBadgesRequest;
import it.cityvoice.api.features.profile.user_badge.dto.UnlockedBadgeResponse;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.shared.exceptions.BadRequestException;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class UserBadgeService {

    private final UserBadgeRepo userBadgeRepo;

    public List<UserBadge> getAllBadges() {
        return userBadgeRepo.findAll();
    }

    public UserBadge getBadgeById(Long id) {
        return userBadgeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Badge non trovato"));
    }

    public UserBadge unlock(UserRome userRome, Badge badge) {
        UserBadge userBadge = new UserBadge();
        userBadge.setUserRome(userRome);
        userBadge.setBadge(badge);
        userBadge.setUnlockedAt(Instant.now());
        return userBadgeRepo.save(userBadge);
    }

    @Transactional(readOnly = true)
    public List<UnlockedBadgeResponse> getUnlockedBadges(UserRome userRome) {
        return userBadgeRepo.findByUserRomeOrderByUnlockedAtAsc(userRome).stream()
                .map(UnlockedBadgeResponse::from)
                .toList();
    }

    @Transactional
    public List<UnlockedBadgeResponse> setFeaturedBadges(UserRome userRome, @Valid FeaturedBadgesRequest request) {
        List<Long> badgeIds = request.badgeIds();
        if (new HashSet<>(badgeIds).size() != badgeIds.size()) {
            throw new BadRequestException("Lo stesso badge non può comparire due volte");
        }

        // azzera tutte le posizioni: i badge caricati dopo arrivano con posizione null
        userBadgeRepo.clearFeaturedPositions(userRome);

        Map<Long, UserBadge> byBadgeId = userBadgeRepo.findByUserRomeAndBadgeIdIn(userRome, badgeIds).stream()
                .collect(Collectors.toMap(userBadge -> userBadge.getBadge().getId(), Function.identity()));
        if (byBadgeId.size() != badgeIds.size()) {
            throw new BadRequestException("Puoi mettere in evidenza solo badge che hai sbloccato");
        }

        // la posizione è l'ordine nella lista: il primo id va al posto 1
        for (int i = 0; i < badgeIds.size(); i++) {
            byBadgeId.get(badgeIds.get(i)).setFeaturedPosition(i + 1);
        }
        return getUnlockedBadges(userRome);
    }

}
