package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.dto.*;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public void register(@RequestBody AuthRequest request) {
        authService.register(request);
    }

    @GetMapping
    public List<AuthRequest> getAllUsers() {
        return authService.getAllUsers();
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody AuthRequest authRequest) {
        return authService.updateUser(id, authRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
    }

    @PatchMapping("/{id}/points")
    public ResponseEntity<User> updateUserPoints(
            @PathVariable Long id,
            @RequestParam Long pointsToAdd) {

        User updatedUser = authService.addPointsToUser(id, pointsToAdd);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.sendPasswordResetToken(request.getEmail());
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }


}
