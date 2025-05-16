package com.reboluxBurger.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    // Ruta base de la carpeta static/images/
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    // Lista de categorías válidas (subcarpetas permitidas)
    private static final List<String> VALID_CATEGORIES = Arrays.asList("burgers", "bebidas", "entrantes", "postres");

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {

        try {
            // Validar que la categoría sea una carpeta conocida
            if (!VALID_CATEGORIES.contains(category)) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Categoría no válida: " + category));
            }

            // Generar un nombre único para el archivo
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Ruta completa del archivo: static/images/<category>/<filename>
            Path categoryPath = Paths.get(UPLOAD_DIR, category);
            Files.createDirectories(categoryPath); // Crea la carpeta si no existe

            Path filepath = categoryPath.resolve(filename);

            // Guardar el archivo
            Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

            // URL pública que se devolverá al frontend
            String imageUrl = "/images/" + category + "/" + filename;

            // Respuesta JSON: { "url": "/images/burgers/abc123.jpg" }
            return ResponseEntity.ok(Map.of("url", imageUrl));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
