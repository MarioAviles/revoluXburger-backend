package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class SupabaseController {

    private final SupabaseStorageService storageService;

    public SupabaseController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    // Subir imagen
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("filename") String filename
    ) {
        try {
            String imageUrl = storageService.uploadImage(file, folder, filename);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error subiendo imagen: " + e.getMessage());
        }
    }

    // Listar imágenes por carpeta
    @GetMapping("/list")
    public ResponseEntity<String> listImages(@RequestParam("folder") String folder) {
        try {
            String images = storageService.listImages(folder);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error listando imágenes: " + e.getMessage());
        }
    }

    // Borrar imagen
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(
            @RequestParam("folder") String folder,
            @RequestParam("filename") String filename
    ) {
        try {
            storageService.deleteImage(folder, filename);
            return ResponseEntity.ok("Imagen eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error eliminando imagen: " + e.getMessage());
        }
    }
}
