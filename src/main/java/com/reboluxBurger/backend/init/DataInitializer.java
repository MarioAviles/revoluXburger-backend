package com.reboluxBurger.backend.init;

import com.reboluxBurger.backend.entity.Category;
import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.MenuRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import com.reboluxBurger.backend.service.CategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.reboluxBurger.backend.enums.Type.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryService categoryService;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryService categoryService, MenuRepository menuRepository,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryService = categoryService;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        Category burger = categoryService.getOrCreateCategory("Burger");
        Category entrante = categoryService.getOrCreateCategory("Entrante");
        Category postre = categoryService.getOrCreateCategory("Postre");
        Category bebida = categoryService.getOrCreateCategory("Bebida");

        System.out.println("Burger category: " + burger);
        System.out.println("Entrante category: " + entrante);
        System.out.println("Postre category: " + postre);
        System.out.println("Bebida category: " + bebida);


        if (menuRepository.count() == 0) {
            menuRepository.saveAll(List.of(
                    new Menu(null, "Furia Urbana", "Explosión callejera de doble carne, cheddar fundido, bacon crujiente y cebolla caramelizada. Una bomba de sabor sin reglas.", burger, Medallon, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Furia_Urbana.webp")
                   ));

            System.out.println("Datos iniciales cargados");
        }
        if (userRepository.findByUsername("anonymous").isEmpty()) {
            User anonymousUser = new User();
            anonymousUser.setUsername("anonymous");
            anonymousUser.setPassword(passwordEncoder.encode("anonymous")); // nunca se va a usar para login real
            anonymousUser.setEmail("anonymousUser@email.com");
            anonymousUser.setRole(Role.USER); // o crea un rol especial si quieres: Role.ANONYMOUS
            userRepository.save(anonymousUser);
        }
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin")); // nunca se va a usar para login real
            adminUser.setEmail("admin@email.com");
            adminUser.setRole(Role.ADMIN); // o crea un rol especial si quieres: Role.ANONYMOUS
            userRepository.save(adminUser);
        }
    }
}

