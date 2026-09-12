package it.cityvoice.api.features.profile.categories.services;

import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.repositories.CategoryRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServ {
    private final CategoryRepo categoryRepo;

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public Category getCategoryByName(String categoryName){
        return categoryRepo.findByName(categoryName).orElseThrow(()-> new EntityNotFoundException("Category not found"));
    }
}
