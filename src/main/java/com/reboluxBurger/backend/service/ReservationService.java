package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.Reservation;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public List<ReservationRequest> getAllReservations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Debes iniciar sesión para ver tus reservas");
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (currentUser.getUsername().equals("anonymous")) {
            throw new RuntimeException("El usuario anónimo no puede ver reservas");
        }

        if (currentUser.getRole() == Role.ADMIN) {
            // Admin ve todas las reservas
            return reservationRepository.findAll().stream()
                    .map(reservation -> new ReservationRequest(
                            reservation.getId(),
                            reservation.getName(),
                            reservation.getDescription(),
                            reservation.getPhone(),
                            reservation.getDate(),
                            reservation.getUser() != null ? reservation.getUser().getId() : null
                    ))
                    .collect(Collectors.toList());
        }

        // Usuario normal ve solo sus reservas
        return reservationRepository.findByUserId(currentUser.getId()).stream()
                .map(reservation -> new ReservationRequest(
                        reservation.getId(),
                        reservation.getName(),
                        reservation.getDescription(),
                        reservation.getPhone(),
                        reservation.getDate(),
                        reservation.getUser().getId()
                ))
                .collect(Collectors.toList());
    }


    public Reservation createReservation(Reservation reservation) {
        // Validación del teléfono
        String phone = reservation.getPhone();
        if (phone == null || !phone.matches("^[679]\\d{8}$")) {
            throw new RuntimeException("El número de teléfono debe tener 9 dígitos y comenzar por 6, 7 o 9");
        }

        // Validación del nombre
        if (reservation.getName() == null || reservation.getName().trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        // Validación de la fecha
        if (reservation.getDate() == null) {
            throw new RuntimeException("La fecha de la reserva es obligatoria");
        }

        try {
            // Usuario autenticado
            User currentUser = getCurrentUser();
            reservation.setUser(currentUser);
        } catch (Exception e) {
            // Usuario no autenticado → asociamos el usuario "anonymous"
            User anonymousUser = userRepository.findByUsername("anonymous")
                    .orElseThrow(() -> new RuntimeException("Usuario anónimo no encontrado"));
            reservation.setUser(anonymousUser);
        }

        return reservationRepository.save(reservation);
    }


    public Reservation updateReservation(Long reservationId, Reservation updatedReservation) {
        Reservation existingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("No existe una reserva con ese id"));
        User currentUser = getCurrentUser();

        // Permitir al propietario o al admin editar
        if ((existingReservation.getUser() != null &&
                existingReservation.getUser().getId().equals(currentUser.getId()))
                || currentUser.getRole() == Role.ADMIN) {

            existingReservation.setDate(updatedReservation.getDate());
            existingReservation.setDescription(updatedReservation.getDescription());
            existingReservation.setName(updatedReservation.getName());
            existingReservation.setPhone(updatedReservation.getPhone());

            return reservationRepository.save(existingReservation);
        }

        throw new RuntimeException("No tienes autorización para actualizar esta reserva");
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        User currentUser = getCurrentUser();

        // Permitir al propietario o al admin borrar
        if ((reservation.getUser() != null &&
                reservation.getUser().getId().equals(currentUser.getId()))
                || currentUser.getRole() == Role.ADMIN) {

            reservationRepository.delete(reservation);
        } else {
            throw new RuntimeException("No tienes autorización para borrar esta reserva");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Usuario no autenticado");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
