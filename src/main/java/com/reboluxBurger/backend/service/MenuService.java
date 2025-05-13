package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.MenuRequest;
import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.MenuRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public MenuService(MenuRepository menuRepository, UserRepository userRepository) {
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    public List<MenuRequest> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuRequest)
                .collect(Collectors.toList());
    }

    public Menu createMenu(Menu menu) {
        User currentUser = getAuthenticatedUser();

        requireAdminRole(currentUser, "No tienes permiso para crear un menú");

        return menuRepository.save(menu);
    }

    public Menu updateMenu(Long id, Menu request) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menú no encontrado con id: " + id));

        User currentUser = getAuthenticatedUser();

        requireAdminRole(currentUser, "No tienes permiso para modificar el menú");

        existingMenu.setName(request.getName());
        existingMenu.setDescription(request.getDescription());
        existingMenu.setCategory(request.getCategory());
        existingMenu.setType(request.getType());
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
                menu.getCategory(),
                menu.getType(),
                menu.getPoints(),
                menu.getImageUrl(),
                menu.getPrice()
        );
    }
}
