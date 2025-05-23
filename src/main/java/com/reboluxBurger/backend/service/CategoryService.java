package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.entity.Category;
import com.reboluxBurger.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Devuelve todas las categorías
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Crea una nueva categoría si no existe otra con ese nombre
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        return categoryRepository.save(category);
    }

    // Borra una categoría por id si existe
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
    }

    // Obtiene una categoría por nombre, o la crea si no existe
    public Category getOrCreateCategory(String name) {
        String normalized = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        Optional<Category> existing = categoryRepository.findByName(normalized);
        return existing.orElseGet(() -> categoryRepository.save(new Category(null, normalized)));
    }

}
