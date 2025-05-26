package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.Reservation;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import com.reboluxBurger.backend.security.CurrentUserProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EmailService emailService;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, CurrentUserProvider currentUserProvider, EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 3 * * *") // Cada día a las 03:00 de la mañana
    public void deleteExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByDateBefore(LocalDateTime.now());
        reservationRepository.deleteAll(expiredReservations);
    }

    public List<ReservationRequest> getAllReservations() {
        User currentUser = currentUserProvider.getCurrentUser();

        List<Reservation> reservations = currentUser.getRole() == Role.ADMIN
                ? reservationRepository.findAll()
                : reservationRepository.findByUserId(currentUser.getId());

        return reservations.stream()
                .map(this::mapToReservationRequest)
                .collect(Collectors.toList());
    }

    public Reservation createReservation(Reservation reservation) {
        validateReservationFields(reservation);

        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser != null) {
            reservation.setUser(currentUser);
        } else {
            // Usuario no autenticado → usar usuario "anonymous"
            User anonymousUser = userRepository.findByUsername("anonymous")
                    .orElseThrow(() -> new RuntimeException("Usuario anónimo no encontrado"));
            reservation.setUser(anonymousUser);
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        // Construir el correo con HTML y estilo inline para mejor compatibilidad
    String subject = "🎉 ¡Tu reserva en Revolux Burger está confirmada! 🍔";

    String text = "<html>" +
        "<body style=\"font-family: Arial, sans-serif; background-color: #fff8e1; margin:0; padding:20px;\">" +
        "<div style=\"max-width:600px; margin:auto; background:#ffffff; border-radius:8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
            "<div style=\"background-color: #fcb300; padding: 20px; border-radius: 8px 8px 0 0; text-align: center;\">" +
                "<h1 style=\"color: #fff; margin: 0; font-size: 24px;\">Revolux Burger 🍔</h1>" +
            "</div>" +
            "<div style=\"padding: 30px; color: #333;\">" +
                "<h2 style=\"color: #fcb300;\">Hola " + savedReservation.getName() + ",</h2>" +
                "<p style=\"font-size: 16px; line-height: 1.5;\">¡Tu mesa está lista y te estamos esperando!</p>" +
                "<p style=\"font-size: 16px; line-height: 1.5;\">" +
                    "Tu reserva ha sido confirmada para el <strong>" +
                    savedReservation.getDate().toLocalDate() + "</strong> a las <strong>" +
                    savedReservation.getDate().toLocalTime() + "</strong>." +
                "</p>" +
                "<p style=\"font-size: 16px; line-height: 1.5;\">" +
                    "Prepárate para disfrutar de las mejores hamburguesas de la ciudad con ingredientes frescos y un ambiente único." +
                "</p>" +
                "<p style=\"font-size: 16px; line-height: 1.5;\">Si tienes alguna duda o quieres modificar tu reserva, no dudes en contactarnos.</p>" +
                "<p style=\"font-weight: bold; font-size: 16px; margin-top: 30px;\">¡Nos vemos pronto en Revolux Burger! 🍔🔥</p>" +
            "</div>" +
            "<div style=\"background-color: #fcb300; padding: 15px; border-radius: 0 0 8px 8px; text-align: center; color: #fff; font-size: 14px;\">" +
                "Revolux Burger - Tu lugar para una experiencia deliciosa" +
            "</div>" +
        "</div>" +
        "</body>" +
        "</html>";

    // Enviar el correo indicando que es HTML
    emailService.sendEmail(savedReservation.getEmail(), subject, text, true);


        return savedReservation;
    }


    public Reservation updateReservation(Long reservationId, Reservation updatedReservation) {
        Reservation existingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("No existe una reserva con ese id"));

        User currentUser = currentUserProvider.getCurrentUser();
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

        User currentUser = currentUserProvider.getCurrentUser();
        validateOwnerOrAdmin(reservation, currentUser, "No tienes autorización para borrar esta reserva");

        reservationRepository.delete(reservation);
    }


    //metodos para gestionar limites de reservas por fecha
    public Map<String, Integer> getReservationCountsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Object[]> counts = reservationRepository.countReservationsGroupedByTimeSlot(start, end);
        Map<String, Integer> result = new HashMap<>();
        for (Object[] row : counts) {
            result.put((String) row[0], ((Long) row[1]).intValue());
        }
        return result;
    }

    private List<LocalTime> generateAllowedTimes() {
        return List.of(
                generateTimeRange(LocalTime.of(13, 0), LocalTime.of(16, 0)),
                generateTimeRange(LocalTime.of(20, 0), LocalTime.of(23, 0))
        ).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private List<LocalTime> generateTimeRange(LocalTime start, LocalTime end) {
        List<LocalTime> result = new java.util.ArrayList<>();
        for (LocalTime time = start; time.isBefore(end); time = time.plusMinutes(30)) {
            result.add(time);
        }
        return result;
    }

    public List<LocalTime> getAvailableTimes(LocalDate date) {
        return generateAllowedTimes().stream()
                .filter(time -> {
                    LocalDateTime start = date.atTime(time);
                    LocalDateTime end = start.plusMinutes(30);
                    int count = reservationRepository.countByDateBetween(start, end);
                    return count < 10;
                })
                .collect(Collectors.toList());
    }

    // Métodos auxiliares



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
        if (reservation.getEmail() == null || !reservation.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("El email no es válido");
        }

        LocalDateTime reservationDateTime = reservation.getDate();
        LocalTime reservationTime = reservationDateTime.toLocalTime();

        // Validar minutos en múltiplos de 15
        int minute = reservationTime.getMinute();
        if (minute % 15 != 0) {
            throw new RuntimeException("La reserva debe ser en intervalos de 15 minutos (minutos permitidos: 00, 15, 30, 45).");
        }

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

        // Limitar a 15 reservas por bloque de 15 minutos
        LocalDateTime startOfBlock = reservationDateTime.withMinute(minute).withSecond(0).withNano(0);
        LocalDateTime endOfBlock = startOfBlock.plusMinutes(30);

        int existingReservations = reservationRepository.countByDateBetween(startOfBlock, endOfBlock.minusNanos(1));
        if (existingReservations >= 10) {
            throw new RuntimeException("Ya hay 5 reservas registradas para este intervalo de 15 minutos. Por favor elige otro horario.");
        }
    }

    private ReservationRequest mapToReservationRequest(Reservation reservation) {
        return new ReservationRequest(
                reservation.getId(),
                reservation.getName(),
                reservation.getDescription(),
                reservation.getPhone(),
                reservation.getDate(),
                reservation.getEmail(),
                reservation.getUser() != null ? reservation.getUser().getId() : null
        );
    }
}
