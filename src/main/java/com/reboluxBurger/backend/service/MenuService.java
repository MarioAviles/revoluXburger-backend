package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.MenuRequest;
import com.reboluxBurger.backend.entity.Category;
import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.entity.Type;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.CategoryRepository;
import com.reboluxBurger.backend.repository.MenuRepository;
import com.reboluxBurger.backend.repository.TypeRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TypeRepository typeRepository;

    public MenuService(MenuRepository menuRepository, UserRepository userRepository, CategoryRepository categoryRepository, TypeRepository typeRepository) {
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
    }

    public List<MenuRequest> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuRequest)
                .collect(Collectors.toList());
    }

    public Menu createMenu(MenuRequest request) {
        User currentUser = getAuthenticatedUser();
        requireAdminRole(currentUser, "No tienes permiso para crear un menú");

        Menu menu = mapToMenuEntity(request);
        return menuRepository.save(menu);
    }


    public Menu updateMenu(Long id, MenuRequest request) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menú no encontrado con id: " + id));

        User currentUser = getAuthenticatedUser();
        requireAdminRole(currentUser, "No tienes permiso para modificar el menú");

        // Buscar la categoría por ID
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        Type type = typeRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        // Actualizar los campos del menú
        existingMenu.setName(request.getName());
        existingMenu.setDescription(request.getDescription());
        existingMenu.setCategory(category);
        existingMenu.setType(type);
        existingMenu.setPoints(request.getPoints());
        existingMenu.setImageUrl(request.getImageUrl());
        existingMenu.setPrice(request.getPrice());

        return menuRepository.save(existingMenu);
    }


    public void deleteMenu(Long id) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getRole() == Role.ADMIN && menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
        }
    }

    // Métodos auxiliares

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if ("anonymous".equals(user.getUsername())) {
            throw new RuntimeException("El usuario anónimo no tiene permisos");
        }

        return user;
    }

    private void requireAdminRole(User user, String errorMessage) {
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException(errorMessage);
        }
    }

    private MenuRequest mapToMenuRequest(Menu menu) {
        return new MenuRequest(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getCategory().getId(),
                menu.getType().getId(),
                menu.getPoints(),
                menu.getImageUrl(),
                menu.getPrice()
        );
    }

    private Menu mapToMenuEntity(MenuRequest dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        Type type = typeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new RuntimeException("Tipo no encontrado con ID: " + dto.getTypeId()));

        return new Menu(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                category,
                type,
                dto.getPrice(),
                dto.getPoints(),
                dto.getImageUrl()
        );
    }

}
