package com.reboluxBurger.backend.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/uploads")
public class UploadImagesController {

    private final Cloudinary cloudinary;

    public UploadImagesController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {

        System.out.println("Intentando subir imagen...");
        System.out.println("Nombre del archivo: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("Tamaño del archivo: " + (file != null ? file.getSize() : "null"));
        System.out.println("Folder recibido: " + folder);

        try {
            Map<String, Object> options = ObjectUtils.asMap("folder", folder);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String imageUrl = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir la imagen: " + e.getMessage()));
        }
    }

    @GetMapping("/images/urls")
    public ResponseEntity<?> getImageUrls(@RequestParam("folder") String folder) {
        try {
            Map result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "max_results", 100
                    )
            );

            List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");
            List<String> urls = resources.stream()
                    .map(resource -> (String) resource.get("secure_url"))
                    .toList();

            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener las imágenes: " + e.getMessage()));
        }
    }





}
