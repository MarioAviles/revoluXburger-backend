package com.reboluxBurger.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboluxBurger.backend.dto.ImageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.bucket}")
    private String BUCKET_NAME;

    @Value("${supabase.apikey}")
    private String SUPABASE_API_KEY;


    private final RestTemplate restTemplate;

    public SupabaseStorageService() {
        this.restTemplate = new RestTemplate();
    }

    // SUBIR imagen
    public String uploadImage(MultipartFile file, String folder, String filename) throws java.io.IOException {
        String path = folder + "/" + filename;
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("apikey", SUPABASE_API_KEY);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, requestEntity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + path;
        } else {
            throw new RuntimeException("Error al subir imagen: " + response.getBody());
        }
    }

    // LISTAR imágenes de una carpeta

    public List<ImageDto> listImages(String folder) {
        String listUrl = SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("apikey", SUPABASE_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Body en formato JSON con el prefijo de la carpeta
        String requestBody = "{\"prefix\": \"" + folder + "/\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                listUrl, HttpMethod.POST, requestEntity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                List<ImageDto> images = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                for (JsonNode node : root) {
                    if (node.has("name")) {
                        String fullName = node.get("name").asText(); // Ej: bebidas/Cerveza.jpg
                        String fileName = fullName.substring(fullName.lastIndexOf("/") + 1);
                        String url = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + folder + "/" + fullName;
                        images.add(new ImageDto(fileName, url));
                    }
                }

                return images;
            } catch (Exception e) {
                throw new RuntimeException("Error procesando la respuesta: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Error al listar imágenes: " + response.getBody());
        }
    }

    // ELIMINAR imagen por carpeta y nombre
    public void deleteImage(String folder, String filename) {
        String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + folder + "/" + filename;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("apikey", SUPABASE_API_KEY);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, requestEntity, String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al eliminar imagen: " + response.getBody());
        }
    }

}
