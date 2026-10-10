package it.cityvoice.api.features.stories.repositories;

import it.cityvoice.api.features.districts.enums.Municipio;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoryRepo extends JpaRepository<Story, UUID>, JpaSpecificationExecutor<Story> {
    @Query("SELECT DISTINCT s.district.municipio FROM Story s WHERE s.userRome = :userRome AND s.status <> :excludedStatus")
    List<Municipio> findDistinctMunicipi(@Param("userRome") UserRome userRome, @Param("excludedStatus") StoryStatus excludedStatus);

    @Query("SELECT COUNT(s) > 0 FROM Story s WHERE lower(trim(s.title)) = lower(trim(:title))")
    boolean existsByNormalizedTitle(@Param("title") String title);

    @Override
    @EntityGraph(attributePaths = {"userRome", "userRome.appUser", "district"})
    Page<Story> findAll(Specification<Story> spec, Pageable pageable);
}