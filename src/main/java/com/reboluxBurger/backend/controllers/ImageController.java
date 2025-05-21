// src/main/java/com/reboluxBurger/backend/controllers/UploadImagesController.java
package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("folder") String folder) {
        try {
            String imageUrl = imageService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir la imagen: " + e.getMessage()));
        }
    }

    @GetMapping("/urls")
    public ResponseEntity<?> getImageUrls(@RequestParam("folder") String folder) {
        try {
            List<String> urls = imageService.getImageUrls(folder);
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener las imágenes: " + e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteImage(@RequestParam("publicId") String publicId) {
        try {
            imageService.deleteImage(publicId);
            return ResponseEntity.ok(Map.of("message", "Imagen eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar la imagen: " + e.getMessage()));
        }
    }
}
