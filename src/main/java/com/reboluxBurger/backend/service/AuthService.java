package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.AuthLoginRequest;
import com.reboluxBurger.backend.dto.AuthRequest;
import com.reboluxBurger.backend.dto.AuthResponse;
import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.PasswordResetToken;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.PasswordResetTokenRepository;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import com.reboluxBurger.backend.security.CurrentUserProvider;
import com.reboluxBurger.backend.security.JwtUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    // Repositorio de usuarios, reservas y tokens de recuperación
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CurrentUserProvider currentUserProvider;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // Constructor que inyecta las dependencias necesarias para el servicio de autenticación
    public AuthService(UserRepository userRepository, ReservationRepository reservationRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, CurrentUserProvider currentUserProvider , EmailService emailService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.currentUserProvider = currentUserProvider;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // Método para login: valida usuario y contraseña, y genera un token JWT si son válidos
    public AuthResponse login(AuthLoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(),(user.get().getPassword())) ) {
            String token = jwtUtil.generateToken(user.get()); // genera token JWT
            return new AuthResponse(token);
        }
        throw new RuntimeException("Credenciales incorrectas");
    }

    // Método para registrar un nuevo usuario en el sistema
    public void register(AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // encripta la contraseña
        user.setEmail(request.getEmail());

        Long points = request.getPoints() == null ? 0 : request.getPoints(); // puntos por defecto: 0
        user.setPoints(points);

        Role role = (request.getRole() == null || request.getRole().describeConstable().isEmpty()) ? Role.USER : request.getRole(); // rol por defecto: USER
        user.setRole(role);

        // Prepara y envía el correo de confirmación
        String subject = "Confirmación de tu registro en Revolux Burger";
        String text = "Estimado/a " + user.getUsername() + ",\n\n" +
                "Nos complace darte la bienvenida a la familia de *Revolux Burger*.\n\n" +
                "Tu registro se ha completado exitosamente el día " +
                LocalDateTime.now().toLocalDate() + " a las " +
                LocalDateTime.now().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + ".\n\n" +
                "A partir de ahora podrás realizar reservas, acumular puntos y disfrutar de nuestras promociones exclusivas para miembros registrados.\n\n" +
                "Si no fuiste tú quien realizó este registro, por favor ignora este mensaje o contáctanos de inmediato.\n\n" +
                "¡Gracias por confiar en nosotros!\n\n" +
                "Atentamente,\n\n" +
                "El equipo de Revolux Burger 🍔";

        try {
            emailService.sendEmail(user.getEmail(), subject, text); // envía el correo de bienvenida
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }

        userRepository.save(user); // guarda el nuevo usuario
    }

    // Devuelve todos los usuarios dependiendo del rol del usuario actual
    public List<AuthRequest> getAllUsers() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No estás autenticado");
        }

        // Si es admin, ve todos los usuarios con sus reservas
        if (currentUser.getRole() == Role.ADMIN) {
            return userRepository.findAll().stream().map(user -> new AuthRequest(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getPoints(), user.getRole(), reservationRepository.findByUserId(user.getId()).stream().map(reservation -> new ReservationRequest(reservation.getId(), reservation.getName(), reservation.getDescription(), reservation.getPhone(), reservation.getDate(), reservation.getNumberOfPersons(), reservation.getEmail(), reservation.getUser().getId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.USER && !currentUser.getUsername().equals("anonymous")) {
            // Si es usuario normal, solo ve sus propios datos
            return userRepository.findByUsername(currentUser.getUsername()).stream().map(user -> new AuthRequest(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getPoints(), user.getRole(), reservationRepository.findByUserId(user.getId()).stream().map(reservation -> new ReservationRequest(reservation.getId(), reservation.getName(), reservation.getDescription(), reservation.getPhone(), reservation.getDate(), reservation.getNumberOfPersons(), reservation.getEmail(), reservation.getUser().getId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("No tienes autorización para mostrar los usuarios");
        }
    }

    // Elimina un usuario si el actual es administrador
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No estás autenticado");
        }

        if (currentUser.getRole() == Role.ADMIN) {
            // Elimina token de recuperación si existe
            passwordResetTokenRepository.findByUser(user)
                    .ifPresent(passwordResetTokenRepository::delete);

            userRepository.delete(user);
        } else {
            throw new RuntimeException("No tienes autorización para borrar este usuario");
        }
    }

    // Permite actualizar los datos de un usuario (a sí mismo o cualquier otro si es admin)
    public User updateUser(Long userId, AuthRequest authRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese id"));

        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No estás autenticado");
        }

        if (existingUser.getId().equals(currentUser.getId()) || currentUser.getRole() == Role.ADMIN) {

            existingUser.setUsername(authRequest.getUsername());
            if (authRequest.getPassword() != null && !authRequest.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(authRequest.getPassword())); // codifica la nueva contraseña
            }
            existingUser.setEmail(authRequest.getEmail());

            // Si es admin, puede modificar rol y puntos
            if (currentUser.getRole() == Role.ADMIN) {
                existingUser.setRole(authRequest.getRole());
                existingUser.setPoints(authRequest.getPoints() != null ? authRequest.getPoints() : existingUser.getPoints());
            }

            return userRepository.save(existingUser); // guarda los cambios
        }
        throw new RuntimeException("No tienes autorización para actualizar este usuario");
    }

    // Añade puntos a un usuario existente
    public User addPointsToUser(Long userId, Long pointsToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPoints() == null) {
            user.setPoints(0L);
        }

        user.setPoints(user.getPoints() + pointsToAdd); // suma los puntos
        return userRepository.save(user);
    }

    // Envía un correo con el token de recuperación de contraseña
    @Async
    public void sendPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe usuario con ese email"));

        // Elimina token anterior si ya existía
        passwordResetTokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString(); // genera token aleatorio

        LocalDateTime expiration = LocalDateTime.now(ZoneId.of("Europe/Madrid")).plusMinutes(30);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationDate(expiration);
        passwordResetTokenRepository.save(resetToken);

        // Construye y envía el email con el enlace de recuperación
        String link = "https://revoluxburger-frontend.vercel.app/reset-password?token=" + token;
        String body = "Hola " + user.getUsername() + ",\n\n" +
                "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" + link + "\n\n" +
                "Este enlace expirará en 30 minutos.";

        try {
            emailService.sendEmail(user.getEmail(), "Restablecer contraseña", body);
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    // Permite al usuario cambiar su contraseña usando un token válido
    public void resetPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(nuevaPassword)); // guarda la nueva contraseña encriptada
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken); // elimina el token usado
    }

    // Tarea programada que elimina tokens de recuperación expirados cada hora
    @Scheduled(fixedRate = 3600000)
    public void eliminarTokensExpirados() {
        passwordResetTokenRepository.deleteAllExpiredSinceNow();
    }

}
