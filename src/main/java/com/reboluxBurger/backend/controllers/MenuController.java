package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.dto.MenuRequest;
import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuRequest> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody MenuRequest request) {
        Menu createdMenu = menuService.createMenu(request);
        return ResponseEntity.ok(createdMenu);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody MenuRequest request) {
        Menu updatedMenu = menuService.updateMenu(id, request);
        return ResponseEntity.ok(updatedMenu);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}
