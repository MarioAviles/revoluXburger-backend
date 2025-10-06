package com.reboluxBurger.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboluxBurger.backend.dto.ImageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Value("${supabase.url:${SUPABASE_URL:}}")
    private String SUPABASE_URL;

    @Value("${supabase.bucket:${SUPABASE_BUCKET:}}")
    private String BUCKET_NAME;

    @Value("${supabase.apikey:${SUPABASE_API_KEY:}}")
    private String SUPABASE_API_KEY;

    @Value("${supabase.service_role_key:${SUPABASE_SERVICE_ROLE_KEY:}}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    private final RestTemplate restTemplate;

    public ImageService() {
        this.restTemplate = new RestTemplate();
    }

    // -------- Helpers ----------
    private HttpHeaders storageHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String key = (SUPABASE_SERVICE_ROLE_KEY != null && !SUPABASE_SERVICE_ROLE_KEY.isBlank())
                ? SUPABASE_SERVICE_ROLE_KEY
                : SUPABASE_API_KEY;
        headers.set("Authorization", "Bearer " + key);
        headers.set("apikey", key);
        return headers;
    }

    private String normalizeFolder(String folder) {
        if (folder == null) return "";
        return folder.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String joinPath(String folder, String filename) {
        folder = normalizeFolder(folder);
        String name = (filename == null) ? "" : filename.replaceAll("^/+", "");
        return folder.isEmpty() ? name : folder + "/" + name;
    }

    private String publicUrl(String path) {
        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + path;
    }

    // -------- Subir ----------
    public String uploadImage(MultipartFile file, String folder) throws java.io.IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank())
            throw new IllegalArgumentException("Nombre de archivo inválido");

        String path = joinPath(folder, filename);
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path;

        HttpHeaders headers = storageHeaders();

        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.set("x-upsert", "true"); // opcional, permite sobrescribir

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, requestEntity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return publicUrl(path);
        } else {
            throw new RuntimeException("Error al subir imagen: " + response.getBody());
        }
    }

    // -------- Listar ----------
    public List<ImageDto> listImages(String folder) {
        String listUrl = SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME;

        HttpHeaders headers = storageHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prefix = normalizeFolder(folder);
        if (!prefix.isEmpty()) prefix = prefix + "/";

        String requestBody = "{\"prefix\":\"" + prefix + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                listUrl, HttpMethod.POST, requestEntity, String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al listar imágenes: " + response.getBody());
        }

        try {
            List<ImageDto> images = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            for (JsonNode node : root) {
                if (node.has("name")) {
                    String nameFromApi = node.get("name").asText();
                    String fileName = nameFromApi.contains("/")
                            ? nameFromApi.substring(nameFromApi.lastIndexOf('/') + 1)
                            : nameFromApi;

                    String url = publicUrl(joinPath(folder, fileName));
                    images.add(new ImageDto(fileName, url));
                }
            }
            return images;
        } catch (Exception e) {
            throw new RuntimeException("Error procesando la respuesta: " + e.getMessage());
        }
    }

    // -------- Borrar ----------
    public void deleteImage(String folder, String filename) {
        try {
            // Codifica nombres para evitar errores con espacios, tildes, etc.
            String encodedFolder = URLEncoder.encode(folder, StandardCharsets.UTF_8);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

            String deleteUrl = SUPABASE_URL + "/storage/v1/object/"
                    + BUCKET_NAME + "/" + encodedFolder + "/" + encodedFilename;

            HttpHeaders headers = storageHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    deleteUrl, HttpMethod.DELETE, requestEntity, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al eliminar imagen: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar imagen: " + e.getMessage());
        }
    }
}
