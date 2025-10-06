package com.reboluxBurger.backend.config;

import com.reboluxBurger.backend.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Inyectamos el filtro JWT personalizado
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // Provee un AuthenticationManager que usará los detalles del usuario definidos en la configuración global
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Codificador de contraseñas usando BCrypt (recomendado por Spring Security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF desactivado (usamos JWT, no sesiones)
                .cors(Customizer.withDefaults()) // Activar configuración CORS definida abajo
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // Permitir acceso a H2 Console
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 🔒 Stateless: sin sesiones

                .authorizeHttpRequests(auth -> auth
                        // Permitir preflight requests (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rutas públicas
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()

                        // Rutas protegidas por JWT
                        .requestMatchers(HttpMethod.PUT, "/auth/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/auth/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/auth/{id}/**").authenticated()

                        // H2 Console sin protección
                        .requestMatchers("/h2-console/**").permitAll()

                        // Documentación abierta
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/*", "/swagger-ui.html").permitAll()

                        // Menú público
                        .requestMatchers(HttpMethod.GET, "/menu").permitAll()

                        // Tipos de producto: GET público, POST/DELETE autenticado
                        .requestMatchers(HttpMethod.GET, "/types").permitAll()
                        .requestMatchers(HttpMethod.POST, "/types").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/types/**").authenticated()

                        // Categorías: GET público, POST/DELETE autenticado
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/categories").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").authenticated()

                        // Gestión de imágenes abierta (depende del contexto, podrías protegerlas)
                        .requestMatchers(HttpMethod.GET, "/images/list").permitAll()
                        .requestMatchers(HttpMethod.POST, "/images/upload").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/images/delete").permitAll()

                        // Reservas: creación y consulta abierta, modificación protegida
                        .requestMatchers(HttpMethod.POST, "/reservations").permitAll()
                        .requestMatchers("/reservations/available-times").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reservations/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/reservations/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reservations/**").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // Agrega el filtro JWT personalizado antes del filtro por defecto de autenticación de Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración CORS global para permitir peticiones desde otros orígenes (ej: frontend separado)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*"); // Permite todos los orígenes (útil para desarrollo)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With")); // Headers permitidos
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type")); // Headers expuestos al frontend
        configuration.setAllowCredentials(true); // Permitir envío de cookies/autenticación

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a todas las rutas

        return source;
    }
}
