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
import com.reboluxBurger.backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    // Repositorios necesarios para acceder a la base de datos
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final TypeRepository typeRepository;
    private final CurrentUserProvider currentUserProvider;

    // Constructor que inyecta las dependencias del servicio
    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository, TypeRepository typeRepository, CurrentUserProvider currentUserProvider) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Devuelve todos los menús en forma de DTO MenuRequest
    public List<MenuRequest> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuRequest) // mapea cada entidad a DTO
                .collect(Collectors.toList());
    }

    // Crea un nuevo menú (solo si es admin)
    public Menu createMenu(MenuRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        requireAdminRole(currentUser, "No tienes permiso para crear un menú");

        Menu menu = mapToMenuEntity(request); // convierte el DTO en entidad
        return menuRepository.save(menu); // guarda el menú
    }

    // Actualiza un menú existente (solo si es admin)
    public Menu updateMenu(Long id, MenuRequest request) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menú no encontrado con id: " + id));

        User currentUser = currentUserProvider.getCurrentUser();
        requireAdminRole(currentUser, "No tienes permiso para modificar el menú");

        // Buscar la categoría del menú
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        // Buscar el tipo si se proporciona
        Type type = null;
        if (request.getTypeId() != null) {
            type = typeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Tipo no encontrado con ID: " + request.getTypeId()));
        }

        // Actualiza los atributos del menú con los valores del DTO
        existingMenu.setName(request.getName());
        existingMenu.setDescription(request.getDescription());
        existingMenu.setCategory(category);
        existingMenu.setType(type);
        existingMenu.setPoints(request.getPoints());
        existingMenu.setImageUrl(request.getImageUrl());
        existingMenu.setPrice(request.getPrice());

        return menuRepository.save(existingMenu); // guarda los cambios
    }

    // Elimina un menú por ID (solo si es admin)
    public void deleteMenu(Long id) {
        User currentUser = currentUserProvider.getCurrentUser();

        // Verifica si es admin
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("No tienes permisos para eliminar el menú");
        }

        if (!menuRepository.existsById(id)) {
            throw new RuntimeException("Menú no encontrado");
        }

        menuRepository.deleteById(id); // elimina el menú
    }

    // Método auxiliar para comprobar si un usuario es administrador
    private void requireAdminRole(User user, String errorMessage) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new RuntimeException(errorMessage);
        }
    }

    // Mapea una entidad Menu a un DTO MenuRequest
    private MenuRequest mapToMenuRequest(Menu menu) {
        return new MenuRequest(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getCategory().getId(),
                menu.getType() != null ? menu.getType().getId() : null,
                menu.getPoints(),
                menu.getImageUrl(),
                menu.getPrice()
        );
    }

    // Convierte un DTO MenuRequest a una entidad Menu
    private Menu mapToMenuEntity(MenuRequest dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        Type type = null;
        if (dto.getTypeId() != null) {
            type = typeRepository.findById(dto.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Tipo no encontrado con ID: " + dto.getTypeId()));
        }

        // Crea una nueva entidad Menu con los datos proporcionados
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
