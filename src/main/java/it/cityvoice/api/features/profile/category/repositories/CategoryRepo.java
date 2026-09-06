package it.cityvoice.api.features.profile.category.repositories;

import it.cityvoice.api.features.profile.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}
