package com.reboluxBurger.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.ApiException;
import com.cloudinary.utils.ObjectUtils;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    public ImageService(Cloudinary cloudinary, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.userRepository = userRepository;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Map<String, Object> options = ObjectUtils.asMap("folder", folder);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) uploadResult.get("secure_url");
        } else {
            throw new RuntimeException("No tienes permisos para subir imágenes");
        }
    }

    public List<String> getImageUrls(String folder) throws Exception {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Map result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "resource_type", "image",
                            "prefix", folder + "/",
                            "max_results", 100
                    )
            );

            List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");

            return resources.stream()
                    .filter(resource -> {
                        String publicId = (String) resource.get("public_id");
                        return publicId != null && publicId.matches(Pattern.quote(folder) + "/[^/]+");
                    })
                    .map(resource -> (String) resource.get("secure_url"))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("No tienes permisos para acceder a estas imágenes");
        }
    }

    public void deleteImage(String publicId) throws IOException, ApiException {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } else {
            throw new RuntimeException("No tienes permisos para eliminar imágenes");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}