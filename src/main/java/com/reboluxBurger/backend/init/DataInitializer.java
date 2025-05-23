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
                    new Menu(null, "Furia Urbana", "Explosión callejera de doble carne, cheddar fundido, bacon crujiente y cebolla caramelizada. Una bomba de sabor sin reglas.", burger, Medallon, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Furia_Urbana.webp"),
                    new Menu(null, "El Capo", "Carne smash doble, doble queso, cebolla morada, pepinillos y alioli mafioso. Puro poder con cada mordida.", burger, Smash, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/El_Capo.webp"),
                    new Menu(null, "Garden Punk", "Veggie rebelde con alcachofa crunchy, verdes frescos y mayonesa de ajo. Para los que comen verde sin ser aburridos.", burger, Vegana, BigDecimal.valueOf(12), 1200L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Garden_Punk.webp"),
                    new Menu(null, "La Bestia", "Triple cheddar, bacon y salsa secreta. Es rápida, intensa y no perdona.", burger, Medallon, BigDecimal.valueOf(11), 1100L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/La_Bestia.webp"),
                    new Menu(null, "El Venerado", "Doble smash, montaña de bacon, cheddar y BBQ dulce. Un culto al cerdo que no perdona.", burger, Smash, BigDecimal.valueOf(12), 1200L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/El_Venerado.webp"),
                    new Menu(null, "Extinción Smash", "Carnes prehistóricas, queso fundido y alioli. Tan brutal como una huella fósil.", burger, Smash, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Extincion_Smash.webp"),
                    new Menu(null, "Fuego Cruzado", "Doble carne, queso cheddar, cebolla morada, salsa picante. Te dispara directo al antojo.", burger, Pollo, BigDecimal.valueOf(11), 1100L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Fuego_Cruzado.webp"),
                    new Menu(null, "La Monster", "Cuádruple carne picada, cheddar en capas, bacon, cebolla crispy y mostaza fuerte. El final boss de las hamburguesas.", burger, Medallon, BigDecimal.valueOf(14), 1400L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/La_Monster.webp"),
                    new Menu(null, "La Patrona", "Picante, poderosa y con estilo. Carne especiada, jalapeños, cheddar y guacamole. Ordena con respeto.", burger, Medallon, BigDecimal.valueOf(13.50), 1350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/La_Patrona.webp"),
                    new Menu(null, "Morena Mía", "Rúcula, queso suave, carne de waygu y cebolla dulce. Elegancia con sabor atrevido.", burger, Medallon, BigDecimal.valueOf(12), 1200L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Morena_Mia.webp"),
                    new Menu(null, "Porcopolis", "Medallon + huevo + bacón + cheddar. Un imperio de las burgers.", burger, Medallon, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Porcopolis.webp"),
                    new Menu(null, "Santa Smash", "Cheddar, lechuga, tomate y mayonesa. La santa trinidad de la hamburguesa bien hecha.", burger, Smash, BigDecimal.valueOf(11.50), 1150L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Santa_Smash.webp"),
                    new Menu(null, "Triple Crimen", "Tres carnes smash, tres quesos y un crimen perfecto de sabor. Imposible salir ileso.", burger, Smash, BigDecimal.valueOf(13.50), 1350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Triple_Crimen.webp"),
                    new Menu(null, "Vinilo & Mostaza", "Revive los 50’s: pepinillos, ketchup, mostaza, cebolla morada, tomate y queso americano. Pura nostalgia entre panes.", burger, Medallon, BigDecimal.valueOf(13.50), 1350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Vinilo_&_Mostaza.webp"),
                    new Menu(null, "Western Nights", "Estilo del viejo oeste: carne jugosa, queso suizo, tomate y salsa ranch. Clásico pero letal.", burger, Medallon, BigDecimal.valueOf(13), 1300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/burgers/Western_Nights.webp"),
                    new Menu(null, "Agua 1L", "Botella de agua de 1 litro", bebida, BigDecimal.valueOf(2.50), 250L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/bebidas/Agua.webp"),
                    new Menu(null, "Cerveza", "Quinto de cerveza 200ml", bebida, BigDecimal.valueOf(3.50), 350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/bebidas/Cerveza.webp"),
                    new Menu(null, "Coca Cola", "Botella de coca cola de 200ml", bebida, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/bebidas/Cocacola.webp"),
                    new Menu(null, "Limonada", "Vaso de limonada", bebida, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/bebidas/Limonada.webp"),
                    new Menu(null, "Helado de Chocolate", "Bola de helado de chocolate", postre, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/BolaHeladoChocolate.webp"),
                    new Menu(null, "Helado de Vainilla", "Bola de helado de vainilla", postre, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/BolaHeladoVainilla.webp"),
                    new Menu(null, "Brownie de Chocolate", "Porción de Brownie de chocolate", postre, BigDecimal.valueOf(3.50), 350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/Brownie.webp"),
                    new Menu(null, "Tarta de Queso", "Porción de tarta de queso", postre, BigDecimal.valueOf(3.50), 350L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/Cheesecake.webp"),
                    new Menu(null, "Mousse", "Mousse de chocolate", postre, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/Mousse.webp"),
                    new Menu(null, "Mousse de la Abuela", "Mousse tradicional con un pequeño toque de la abuela", postre, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/MoussedelaAbuela.webp"),
                    new Menu(null, "Mousse de Pistacho", "Mousse de pistacho", postre, BigDecimal.valueOf(3), 300L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/postres/MoussedePistacho.webp"),
                    new Menu(null, "Alitas del Bronx", "Alas tiernas con actitud. Jugosas por dentro, doradas por fuera y con una salsa que te va a hacer sudar. No aptas para paladares tímidos.", entrante, BigDecimal.valueOf(7), 700L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/AlitasdelBronx.webp"),
                    new Menu(null, "Aritos", "Aros de cebolla gigantes, dorados como el verano y crujientes como el primer mordisco de la libertad. Un clásico que no pide permiso.", entrante, BigDecimal.valueOf(6), 600L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/Aritos.webp"),
                    new Menu(null, "Nachorreo", "Un volcán de nachos bañados en queso fundido, jalapeños rebeldes, guacamole con flow y crema agria. El caos que te mereces.", entrante, BigDecimal.valueOf(7.50), 750L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/Nachorreo.webp"),
                    new Menu(null, "Pollokids", " Crujientes bastoncitos de pollo empanado que no juzgan si tienes alma de niño. Bañalos en lo que quieras... o atrévete a comerlos solos. Porque cuando algo está bien hecho, no necesita acompañante.", entrante, BigDecimal.valueOf(5), 500L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/Pollokids.webp"),
                    new Menu(null, "Tequeños", "Palitos venezolanos rellenos de queso que se derrite en tu boca (y en tu alma). Para mojar en sala... y perder el control.", entrante, BigDecimal.valueOf(6.50), 650L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/Tequenos.webp"),
                    new Menu(null, "TequeñosDeluxe", "El arte del queso y lo dulce en un solo bocado. Inspirados en Da Vinci, hechos para genios del antojo. Dales un mordisco y pinta tu día de sabor.", entrante, BigDecimal.valueOf(7), 700L, "https://amatliizljqlgceescos.supabase.co/storage/v1/object/public/images/entrantes/TequenosDeluxe.webp")
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

