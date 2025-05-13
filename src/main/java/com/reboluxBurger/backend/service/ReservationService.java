package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.Reservation;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Scheduled(cron = "0 0 3 * * *") // Cada día a las 03:00 de la mañana
    public void deleteExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByDateBefore(LocalDateTime.now());
        reservationRepository.deleteAll(expiredReservations);
    }

    public List<ReservationRequest> getAllReservations() {
        User currentUser = getCurrentUser();

        List<Reservation> reservations = currentUser.getRole() == Role.ADMIN
                ? reservationRepository.findAll()
                : reservationRepository.findByUserId(currentUser.getId());

        return reservations.stream()
                .map(this::mapToReservationRequest)
                .collect(Collectors.toList());
    }

    public Reservation createReservation(Reservation reservation) {
        validateReservationFields(reservation);

        try {
            reservation.setUser(getCurrentUser());
        } catch (Exception e) {
            // Si no está autenticado, asociamos el usuario "anonymous"
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
        validateOwnerOrAdmin(existingReservation, currentUser, "No tienes autorización para actualizar esta reserva");

        validateReservationFields(updatedReservation);

        existingReservation.setDate(updatedReservation.getDate());
        existingReservation.setDescription(updatedReservation.getDescription());
        existingReservation.setName(updatedReservation.getName());
        existingReservation.setPhone(updatedReservation.getPhone());

        return reservationRepository.save(existingReservation);
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        User currentUser = getCurrentUser();
        validateOwnerOrAdmin(reservation, currentUser, "No tienes autorización para borrar esta reserva");

        reservationRepository.delete(reservation);
    }

    // Métodos auxiliares

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null  || username.equals("anonymous")) {
            throw new RuntimeException("Usuario no autenticado");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void validateOwnerOrAdmin(Reservation reservation, User user, String errorMessage) {
        boolean isOwner = reservation.getUser() != null && reservation.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException(errorMessage);
        }
    }

    private void validateReservationFields(Reservation reservation) {
        if (reservation.getPhone() == null || !reservation.getPhone().matches("^[679]\\d{8}$")) {
            throw new RuntimeException("El número de teléfono debe tener 9 dígitos y comenzar por 6, 7 o 9");
        }
        if (reservation.getName() == null || reservation.getName().trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (reservation.getDate() == null) {
            throw new RuntimeException("La fecha de la reserva es obligatoria");
        }

        LocalDateTime reservationDateTime = reservation.getDate();
        LocalTime reservationTime = reservationDateTime.toLocalTime();

        boolean isLunch = !reservationTime.isBefore(LocalTime.of(13, 0)) &&
                reservationTime.isBefore(LocalTime.of(16, 0));

        boolean isDinner = !reservationTime.isBefore(LocalTime.of(20, 0)) &&
                reservationTime.isBefore(LocalTime.of(23, 0));

        //si está fuera de los horarios de comida o cena no se puede reservar
        if (!isLunch && !isDinner) {
            throw new RuntimeException("La hora de la reserva debe estar entre las 13:00-16:00 o 20:00-23:00");
        }

        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La reserva no puede ser en el pasado");
        }
    }

    private ReservationRequest mapToReservationRequest(Reservation reservation) {
        return new ReservationRequest(
                reservation.getId(),
                reservation.getName(),
                reservation.getDescription(),
                reservation.getPhone(),
                reservation.getDate(),
                reservation.getUser() != null ? reservation.getUser().getId() : null
        );
    }
}
