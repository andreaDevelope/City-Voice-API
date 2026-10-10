package it.cityvoice.api.features.stories.specifications;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.districts.enums.Municipio;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public final class StorySpecifications {

    private StorySpecifications() {
    }

    public static Specification<Story> published() {
        return (root, query, cb) -> cb.equal(root.get("status"), StoryStatus.PUBLISHED);
    }

    public static Specification<Story> ofCategory(String category) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("type"), StoryType.REPORT),
                cb.equal(cb.lower(root.get("category")), category.toLowerCase())
        );
    }

    public static Specification<Story> matches(String q, Collection<Municipio> municipi) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Story, District> district = root.join("district", JoinType.LEFT);

            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            Predicate usernameMatch = cb.like(cb.lower(root.get("userRome").get("appUser").get("username")), pattern);
            Predicate districtNameMatch = cb.like(cb.lower(district.get("name")), pattern);
            Predicate anyMatch = cb.or(titleMatch, usernameMatch, districtNameMatch);

            if (!municipi.isEmpty()) {
                anyMatch = cb.or(anyMatch, district.get("municipio").in(municipi));
            }
            return anyMatch;
        };
    }
}
