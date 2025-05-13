package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.MenuRequest;
import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuRequest> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuRequest)
                .collect(Collectors.toList());
    }

    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    public Menu updateMenu(Long id, Menu request) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menú no encontrado con id: " + id));

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
        if (menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
        } else {
            throw new RuntimeException("Menú no encontrado con id: " + id);
        }
    }

    // De entidad a DTO
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
