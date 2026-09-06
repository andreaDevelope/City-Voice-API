package it.cityvoice.api.features.profile.badge.services;

import it.cityvoice.api.features.profile.badge.dto.BadgeDto;
import it.cityvoice.api.features.profile.badge.dto.CategoryProgressResponse;
import it.cityvoice.api.features.profile.badge.entity.Badge;
import it.cityvoice.api.features.profile.badge.repositories.BadgeRepo;
import it.cityvoice.api.features.profile.category.entity.Category;
import it.cityvoice.api.features.profile.category.repositories.CategoryRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeServ {

    private final BadgeRepo badgeRepo;
    private final CategoryRepo categoryRepo;

    public CategoryProgressResponse getProgressForCategory(String categoryName, int counter) {
        Category category = categoryRepo.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata: " + categoryName));

        List<Badge> badges = badgeRepo.findByCategoryIdOrderBySequenceOrderAsc(category.getId());

        Badge current = badges.stream()
                .filter(b -> b.getMissionThreshold() > counter)
                .findFirst()
                .orElse(badges.isEmpty() ? null : badges.get(badges.size() - 1));

        if (current == null) {
            throw new ResourceNotFoundException("Nessun badge configurato per la categoria: " + categoryName);
        }

        int currentIndex = badges.indexOf(current);
        Badge next = (currentIndex + 1 < badges.size()) ? badges.get(currentIndex + 1) : null;

        return new CategoryProgressResponse(
                categoryName,
                toDto(current),
                next != null ? toDto(next) : null,
                counter
        );

    }

    private BadgeDto toDto(Badge badge) {
        return new BadgeDto(badge.getId(), badge.getName(), badge.getDescription(), badge.getMissionThreshold(), badge.getSequenceOrder());
    }

    public CategoryProgressResponse getProgressForUser(UserRome userRome, String categoryName) {
        int counter = resolveCounter(userRome, categoryName);
        return getProgressForCategory(categoryName, counter);
    }

    private int resolveCounter(UserRome userRome, String categoryName) {
        return switch (categoryName) {
            case "activity" -> userRome.getActivityCounter();
            case "neighborhood" -> userRome.getNeighborhoodCounter();
            case "continuity" -> userRome.getContinuityCounter();
            case "impact" -> userRome.getImpactCounter();
            default -> throw new ResourceNotFoundException("Categoria non valida: " + categoryName);
        };
    }

}
