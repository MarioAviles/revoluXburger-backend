package com.reboluxBurger.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String keepAlive() {
        return "Backend activo ✅";
    }
}
