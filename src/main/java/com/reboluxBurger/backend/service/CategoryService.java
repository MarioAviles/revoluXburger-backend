package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.entity.Category;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.CategoryRepository;
import com.reboluxBurger.backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Devuelve todas las categorías
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Crea una nueva categoría si no existe otra con ese nombre
    public Category createCategory(Category category) {

        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            if (categoryRepository.existsByName(category.getName())) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
            return categoryRepository.save(category);
        } else {
            throw new RuntimeException("No tienes permisos para crear una categoría");
        }
    }

    // Borra una categoría por id si existe
    public void deleteCategory(Long id) {

        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            if (!categoryRepository.existsById(id)) {
                throw new RuntimeException("Categoría no encontrada");
            }
            categoryRepository.deleteById(id);
            } else {
                throw new RuntimeException("No tienes permisos para eliminar una categoría");
            }
    }

    // Obtiene una categoría por nombre, o la crea si no existe
    public Category getOrCreateCategory(String name, String imageUrl) {
        String normalized = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        Optional<Category> existing = categoryRepository.findByName(normalized);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            return categoryRepository.save(new Category(null, normalized, imageUrl));
        }
    }
}
