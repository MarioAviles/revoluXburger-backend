package com.reboluxBurger.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.ApiException;
import com.cloudinary.utils.ObjectUtils;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.UserRepository;
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
        checkAdminAccess(); // Validación de rol
        Map<String, Object> options = ObjectUtils.asMap("folder", folder);
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }

    public List<String> getImageUrls(String folder) throws Exception {
        checkAdminAccess(); // Validación de rol
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
    }

    public void deleteImage(String publicId) throws IOException, ApiException {
        checkAdminAccess(); // ⚠️ Validación de rol
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private void checkAdminAccess() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("No tienes permisos para realizar esta acción");
        }
    }
}
