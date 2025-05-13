package com.reboluxBurger.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // CORS para toda la API
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "https://revoluxburger-frontend.vercel.app"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // Swagger y documentación
                registry.addMapping("/v3/api-docs")
                        .allowedOrigins("http://localhost:5173", "https://revoluxburger-frontend.vercel.app")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);

                registry.addMapping("/swagger-ui/**")
                        .allowedOrigins("http://localhost:5173", "https://revoluxburger-frontend.vercel.app")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}
