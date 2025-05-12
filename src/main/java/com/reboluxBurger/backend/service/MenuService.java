package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.MenuRequest;
import com.reboluxBurger.backend.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

//    public List<MenuRequest> getAllMenus() {
//        return menuRepository.findAll();
//    }

}
