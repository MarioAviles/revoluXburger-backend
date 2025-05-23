package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.AuthLoginRequest;
import com.reboluxBurger.backend.dto.AuthRequest;
import com.reboluxBurger.backend.dto.AuthResponse;
import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import com.reboluxBurger.backend.security.CurrentUserProvider;
import com.reboluxBurger.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository; //llamo a donde se almacenan los usuarios
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder; //llamo al codificador de contraseñas
    private final JwtUtil jwtUtil; //llamo a jwt
    private final CurrentUserProvider currentUserProvider;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, ReservationRepository reservationRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, CurrentUserProvider currentUserProvider , EmailService emailService) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.currentUserProvider = currentUserProvider;
        this.emailService = emailService;
    }

    public AuthResponse login(AuthLoginRequest request) { //le paso por parametro lo que me envian por post
        Optional<User> user = userRepository.findByUsername(request.getUsername()); //cojo el nombre que me han pasado por parametro y uso la funcion para buscarlo
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(),(user.get().getPassword())) ) { //si el usuario existe y la contraseña que me han pasado por parametro coincide con la del usuario
            String token = jwtUtil.generateToken(user.get()); //me genera un token y me lo manda por respuesta
            return new AuthResponse(token);
        }
        throw new RuntimeException("Credenciales incorrectas"); //sino da error
    }

    public void register(AuthRequest request) { //le paso por parametro los datos
        if (userRepository.findByUsername(request.getUsername()).isPresent()) { //busco el nombre para ver si ya existe el usuario
            throw new RuntimeException("El usuario ya existe"); //si existe salta un error
        }
        User user = new User(); //sino crea un nuevo usuario y asigna los datos
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        Long points = request.getPoints() == null ? 0 : request.getPoints();
        user.setPoints(points);

        Role role = (request.getRole() == null || request.getRole().describeConstable().isEmpty()) ? Role.USER : request.getRole();
        user.setRole(role);

        String subject = "Confirmación de tu registro en Rebolux Burger";
        String text = "Hola " + user.getUsername() + ",\n\n" +
                "Te has registrado en Revoluxburger el día " +
                LocalDateTime.now().toLocalDate() + " a las " +
                LocalDateTime.now().toLocalTime() + ".\n\n" +
                "Gracias por unirte a nosotros.\n\nRebolux Burger 🍔";

        emailService.sendEmail(user.getEmail(), subject, text);

        userRepository.save(user);
    }

    public List<AuthRequest> getAllUsers() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No estás autenticado");
        }
        if (currentUser.getRole() == Role.ADMIN) { //si el usuario es admin ve todos los usuarios
            return userRepository.findAll().stream().map(user -> new AuthRequest(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getPoints(), user.getRole(), reservationRepository.findByUserId(user.getId()).stream().map(reservation -> new ReservationRequest(reservation.getId(), reservation.getName(), reservation.getDescription(), reservation.getPhone(), reservation.getDate(), reservation.getEmail(), reservation.getUser().getId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.USER && !currentUser.getUsername().equals("anonymous")) { //si el usuario es un usuario normal y no es anónimo ve sus propios datos
            return userRepository.findByUsername(currentUser.getUsername()).stream().map(user -> new AuthRequest(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getPoints(), user.getRole(), reservationRepository.findByUserId(user.getId()).stream().map(reservation -> new ReservationRequest(reservation.getId(), reservation.getName(), reservation.getDescription(), reservation.getPhone(), reservation.getDate(), reservation.getEmail(), reservation.getUser().getId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("No tienes autorización para mostrar los usuarios");
        }
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No estás autenticado");
        }
        if (currentUser.getRole() == Role.ADMIN) {
            userRepository.delete(user);
        } else {
            throw new RuntimeException("No tienes autorización para borrar este usuario");
        }
    }

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
                existingUser.setPassword(passwordEncoder.encode(authRequest.getPassword()));
            }
            existingUser.setEmail(authRequest.getEmail());

            if (currentUser.getRole() == Role.ADMIN) {
                existingUser.setRole(authRequest.getRole());
                existingUser.setPoints(authRequest.getPoints() != null ? authRequest.getPoints() : existingUser.getPoints());
            }

            return userRepository.save(existingUser);
        }
        throw new RuntimeException("No tienes autorización para actualizar este usuario");
    }
}
