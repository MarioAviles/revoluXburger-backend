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
public class ImageService {

    @Value("${supabase.url}") // Le da el valor de la propiedad supabase.url desde application.properties
    private String SUPABASE_URL;

    @Value("${supabase.bucket}") // Le da el nombre del bucket
    private String BUCKET_NAME;

    @Value("${supabase.apikey}") // Le da la API key
    private String SUPABASE_API_KEY;

    private final RestTemplate restTemplate;

    // Constructor que inicializa RestTemplate (cliente HTTP para hacer peticiones REST)
    public ImageService() {
        this.restTemplate = new RestTemplate();
    }

    // SUBIR imagen a Supabase Storage
    public String uploadImage(MultipartFile file, String folder) throws java.io.IOException {

        String filename = file.getOriginalFilename();

        String path = folder + "/" + filename; // Construye la ruta completa del archivo dentro del bucket

        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path; // Construye la URL para hacer el PUT a Supabase

        // Crea los headers necesarios para la autenticación y el tipo de contenido
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY); // Token de autorización
        headers.set("apikey", SUPABASE_API_KEY); // API Key adicional para Supabase
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Tipo binario para archivos

        // Crea el cuerpo de la petición con los bytes del archivo y los headers
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        // Ejecuta la petición PUT para subir el archivo
        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, requestEntity, String.class
        );

        // Si la subida fue exitosa, devuelve la URL pública del archivo
        if (response.getStatusCode().is2xxSuccessful()) {
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + path;
        } else {
            // Si falló, lanza una excepción con el error
            throw new RuntimeException("Error al subir imagen: " + response.getBody());
        }
    }

    // LISTAR imágenes de una carpeta
    public List<ImageDto> listImages(String folder) {
        // URL para listar archivos del bucket
        String listUrl = SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME;

        // Headers con autenticación y tipo de contenido JSON
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("apikey", SUPABASE_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Cuerpo de la petición con el prefijo de la carpeta a listar
        String requestBody = "{\"prefix\": \"" + folder + "/\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Realiza la petición POST para obtener la lista de archivos
        ResponseEntity<String> response = restTemplate.exchange(
                listUrl, HttpMethod.POST, requestEntity, String.class
        );

        // Si la respuesta es exitosa
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                List<ImageDto> images = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                // Parsear la respuesta JSON a un árbol de nodos
                JsonNode root = mapper.readTree(response.getBody());

                // Itera sobre los nodos (archivos encontrados)
                for (JsonNode node : root) {
                    if (node.has("name")) {
                        String fullName = node.get("name").asText(); // Ej: bebidas/Cerveza.jpg
                        String fileName = fullName.substring(fullName.lastIndexOf("/") + 1); // Extrae solo el nombre del archivo
                        // Construye la URL pública
                        String url = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + folder + "/" + fullName;
                        // Agrega el objeto ImageDto a la lista
                        images.add(new ImageDto(fileName, url));
                    }
                }

                // Devuelve la lista de imágenes encontradas
                return images;
            } catch (Exception e) {
                // Si hubo un error al parsear la respuesta
                throw new RuntimeException("Error procesando la respuesta: " + e.getMessage());
            }
        } else {
            // Si la petición falla
            throw new RuntimeException("Error al listar imágenes: " + response.getBody());
        }
    }

    // ELIMINAR una imagen por carpeta y nombre de archivo
    public void deleteImage(String folder, String filename) {
        // Construye la URL para eliminar el archivo
        String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + folder + "/" + filename;

        // Headers de autenticación
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("apikey", SUPABASE_API_KEY);

        // Crea la entidad de la petición sin cuerpo (solo headers)
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Ejecuta la petición DELETE
        ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, requestEntity, String.class
        );

        // Si la respuesta no es exitosa, lanza una excepción
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al eliminar imagen: " + response.getBody());
        }
    }
}
