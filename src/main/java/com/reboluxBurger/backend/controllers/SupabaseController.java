package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.dto.ImageDto;
import com.reboluxBurger.backend.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class SupabaseController {

    private final SupabaseStorageService storageService;

    public SupabaseController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    // Subir imagen
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("filename") String filename
    ) {
        try {
            String imageUrl = storageService.uploadImage(file, folder, filename);
            return ResponseEntity.ok().body(Map.of("url", imageUrl)); // ✅ JSON {"url": "..."}
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error subiendo imagen: " + e.getMessage()));
        }
    }

    // Listar imágenes por carpeta
    @GetMapping("/list")
    public ResponseEntity<?> listImages(@RequestParam("folder") String folder) {
        try {
            List<ImageDto> images = storageService.listImages(folder);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error listando imágenes: " + e.getMessage());
        }
    }



    // Borrar imagen
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteImage(
            @RequestParam("folder") String folder,
            @RequestParam("filename") String filename
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            String publicId = folder + "/" + filename;
            storageService.deleteImage(publicId);
            response.put("message", "Imagen eliminada correctamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error eliminando imagen: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


}
