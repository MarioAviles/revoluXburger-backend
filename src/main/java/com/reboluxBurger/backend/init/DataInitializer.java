package com.reboluxBurger.backend.init;

import com.reboluxBurger.backend.entity.Menu;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.MenuRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.reboluxBurger.backend.enums.Category.*;
import static com.reboluxBurger.backend.enums.Type.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(MenuRepository menuRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (menuRepository.count() == 0) {
            menuRepository.saveAll(List.of(
                    new Menu(null, "Furia Urbana", "Explosión callejera de doble carne, cheddar fundido, bacon crujiente y cebolla caramelizada. Una bomba de sabor sin reglas.", Burger, Medallon, BigDecimal.valueOf(11.50), 1150L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Furia_Urbana_yurexm.png"),
                    new Menu(null, "El Capo", "Carne smash doble, doble queso, cebolla morada, pepinillos y alioli mafioso. Puro poder con cada mordida.", Burger, Smash, BigDecimal.valueOf(11.50),1150L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/El_Capo_y4csv0.png"),
                    new Menu(null, "Garden Punk", "Veggie rebelde con alcachofa crunchy, verdes frescos y mayonesa de ajo. Para los que comen verde sin ser aburridos.", Burger, Vegana, BigDecimal.valueOf(12),1200L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Garden_Punk_segvgp.jpg"),
                    new Menu(null, "La Bestia", "Triple cheddar, bacon y salsa secreta. Es rápida, intensa y no perdona.", Burger, Medallon, BigDecimal.valueOf(11),1100L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/La_Bestia_xxe6xo.png"),
                    new Menu(null, "El Venerado", "Doble smash, montaña de bacon, cheddar y BBQ dulce. Un culto al cerdo que no perdona.", Burger, Smash, BigDecimal.valueOf(12),1200L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/El_Venerado_xtrfho.png"),
                    new Menu(null, "Extinción Smash", "Carnes prehistóricas, queso fundido y alioli. Tan brutal como una huella fósil.", Burger, Smash, BigDecimal.valueOf(11.50), 1150L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475704/Extincion_Smash_hljtzv.png"),
                    new Menu(null, "Fuego Cruzado", "Doble carne, queso cheddar, cebolla morada, salsa picante. Te dispara directo al antojo.", Burger, Pollo, BigDecimal.valueOf(11), 1100L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Fuego_Cruzado_m8sleo.png"),
                    new Menu(null, "La Monster", "Cuádruple carne picada, cheddar en capas, bacon, cebolla crispy y mostaza fuerte. El final boss de las hamburguesas.", Burger, Medallon, BigDecimal.valueOf(14), 1400L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/La_Monster_znyow8.jpg"),
                    new Menu(null, "La Patrona", "Picante, poderosa y con estilo. Carne especiada, jalapeños, cheddar y guacamole. Ordena con respeto.", Burger, Medallon, BigDecimal.valueOf(13.50), 1350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/La_Patrona_lkalma.jpg"),
                    new Menu(null, "Morena Mía", "Rúcula, queso suave, carne de waygu y cebolla dulce. Elegancia con sabor atrevido.", Burger, Medallon, BigDecimal.valueOf(12), 1200L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Morena_Mia_kbuh7w.png"),
                    new Menu(null, "Porcopolis", "Medallon + huevo + bacón + cheddar. Un imperio de las burgers.", Burger, Medallon, BigDecimal.valueOf(11.50), 1150L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Porcopolis_vkp5mk.png"),
                    new Menu(null, "Santa Smash", "Cheddar, lechuga, tomate y mayonesa. La santa trinidad de la hamburguesa bien hecha.", Burger, Smash, BigDecimal.valueOf(11.50), 1150L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Santa_Smash_cevm4f.png"),
                    new Menu(null, "Triple Crimen", "Tres carnes smash, tres quesos y un crimen perfecto de sabor. Imposible salir ileso.", Burger, Smash, BigDecimal.valueOf(13.50), 1350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Triple_Crimen_yx0wwn.png"),
                    new Menu(null, "Vinilo & Mostaza", "Revive los 50’s: pepinillos, ketchup, mostaza, cebolla morada, tomate y queso americano. Pura nostalgia entre panes.", Burger, Medallon, BigDecimal.valueOf(13.50), 1350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Vinilo___Mostaza_vod0tw.png"),
                    new Menu(null, "Western Nights", "Estilo del viejo oeste: carne jugosa, queso suizo, tomate y salsa ranch. Clásico pero letal.", Burger, Medallon, BigDecimal.valueOf(13), 1300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475702/Western_Nights_qnjdmz.png"),
                    new Menu(null, "Agua 1L", "Botella de agua de 1 litro", Bebida, BigDecimal.valueOf(2.50), 250L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Agua_mqjyex.png"),
                    new Menu(null, "Cerveza", "Quinto de cerveza 200ml", Bebida, BigDecimal.valueOf(3.50), 350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Cerveza_ftupod.jpg"),
                    new Menu(null, "Coca Cola", "Botella de coca cola de 200ml", Bebida, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Cocacola_eocvxk.jpg"),
                    new Menu(null, "Limonada", "Vaso de limonada", Bebida, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475701/Limonada_bathus.png"),
                    new Menu(null, "Helado de Chocolate", "Bola de helado de chocolate", Postre, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/BolaHeladoChocolate_hlom5a.jpg"),
                    new Menu(null, "Helado de Vainilla", "Bola de helado de vainilla", Postre, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/BolaHeladoVainilla_oufim9.png"),
                    new Menu(null, "Brownie de Chocolate", "Porción de Brownie de chocolate", Postre, BigDecimal.valueOf(3.50), 350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Brownie_mobf3e.jpg"),
                    new Menu(null, "Tarta de Queso", "Porción de tarta de queso", Postre, BigDecimal.valueOf(3.50), 350L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Cheesecake_uwrbv0.jpg"),
                    new Menu(null, "Mousse", "Mousse de chocolate", Postre, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Mousse_is4lvv.png"),
                    new Menu(null, "Mousse de la Abuela", "Mousse tradicional con un pequeño toque de la abuela", Postre, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475704/MoussedelaAbuela_uxvjmr.png"),
                    new Menu(null, "Mousse de Pistacho", "Mousse de pistacho", Postre, BigDecimal.valueOf(3), 300L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475704/MoussedePistacho_df1y8t.png"),
                    new Menu(null, "Alitas del Bronx", "Alas tiernas con actitud. Jugosas por dentro, doradas por fuera y con una salsa que te va a hacer sudar. No aptas para paladares tímidos.", Entrante, BigDecimal.valueOf(7), 700L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/AlitasdelBronx_wqds5e.png"),
                    new Menu(null, "Aritos", "Aros de cebolla gigantes, dorados como el verano y crujientes como el primer mordisco de la libertad. Un clásico que no pide permiso.", Entrante, BigDecimal.valueOf(6), 600L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Aritos_eypwfn.png"),
                    new Menu(null, "Nachorreo", "Un volcán de nachos bañados en queso fundido, jalapeños rebeldes, guacamole con flow y crema agria. El caos que te mereces.", Entrante, BigDecimal.valueOf(7.50), 750L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Nachorreo_xigxlk.png"),
                    new Menu(null, "Pollokids", " Crujientes bastoncitos de pollo empanado que no juzgan si tienes alma de niño. Bañalos en lo que quieras... o atrévete a comerlos solos. Porque cuando algo está bien hecho, no necesita acompañante.", Entrante, BigDecimal.valueOf(5), 500L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Pollokids_xv4zjq.jpg"),
                    new Menu(null, "Tequeños", "Palitos venezolanos rellenos de queso que se derrite en tu boca (y en tu alma). Para mojar en sala... y perder el control.", Entrante, BigDecimal.valueOf(6.50), 650L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Teque%C3%B1os_o3avsm.png"),
                    new Menu(null, "TequeñosDeluxe", "El arte del queso y lo dulce en un solo bocado. Inspirados en Da Vinci, hechos para genios del antojo. Dales un mordisco y pinta tu día de sabor.", Entrante, BigDecimal.valueOf(7), 700L, "https://res.cloudinary.com/ddxoloq91/image/upload/v1747475703/Teque%C3%B1osDeluxe_e7247r.jpg")
                    
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

