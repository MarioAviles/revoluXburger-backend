package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.Reservation;
import com.reboluxBurger.backend.entity.User;
import com.reboluxBurger.backend.enums.Role;
import com.reboluxBurger.backend.repository.ReservationRepository;
import com.reboluxBurger.backend.repository.UserRepository;
import com.reboluxBurger.backend.security.CurrentUserProvider;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
                    .orElseThrow(() -> new IllegalStateException("Usuario anónimo no encontrado"));
            reservation.setUser(anonymousUser);
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        // Crear enlace para agregar a Google Calendar

        LocalDateTime start = savedReservation.getDate();
        LocalDateTime end = start.plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String startTime = start.format(formatter);
        String endTime = end.format(formatter);

        String calendarUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + java.net.URLEncoder.encode("Reserva en Revolux Burger", java.nio.charset.StandardCharsets.UTF_8)
                + "&dates=" + startTime + "/" + endTime
                + "&details=" + java.net.URLEncoder.encode("Reserva en Revolux Burger para " + savedReservation.getName(), java.nio.charset.StandardCharsets.UTF_8)
                + "&location=" + java.net.URLEncoder.encode("Revolux Burger, Calle Carlos III", java.nio.charset.StandardCharsets.UTF_8)
                + "&sf=true&output=xml";


        // Enviar correo de confirmación
        String subject = "Confirmación de tu reserva en Revolux Burger";

        String text = "Estimado/a " + savedReservation.getName() + ",\n\n" +
                "Te agradecemos por elegir Revolux Burger para tu próxima visita. Nos complace informarte que tu reserva ha sido registrada exitosamente en nuestro sistema.\n\n" +
                "Aquí tienes los detalles de tu reserva:\n\n" +
                "📅 Fecha: " + savedReservation.getDate().toLocalDate() + "\n" +
                "⏰ Hora: " + savedReservation.getDate().toLocalTime() + "\n" +
                "👤 Nombre de la reserva: " + savedReservation.getName() + "\n" +
                "📧 Correo de contacto: " + savedReservation.getEmail() + "\n\n" +
                "Tu mesa estará lista a tu llegada. Nos esforzamos por ofrecer una experiencia gastronómica de alta calidad, con un ambiente acogedor y un servicio excepcional.\n\n" +
                "Si necesitas realizar algún cambio o cancelar tu reserva, te pedimos que nos contactes con al menos 24 horas de antelación, respondiendo a este correo o escribiéndonos directamente a revoluxburger@gmail.com.\n\n" +
                "Recuerda que también puedes seguirnos en nuestras redes sociales para mantenerte al tanto de nuestras promociones y novedades.\n\n" +
                "Gracias por confiar en nosotros. Será un placer atenderte.\n\n" +
                "🗓 Puedes agregar esta reserva a tu Google Calendar haciendo clic en el siguiente enlace:\n" +
                calendarUrl + "\n\n" +
                "Atentamente,\n\n" +
                "Equipo Revolux Burger 🍔\n";

        emailService.sendEmail(savedReservation.getEmail(), subject, text);

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
        existingReservation.setNumberOfPersons(updatedReservation.getNumberOfPersons());


        return reservationRepository.save(existingReservation);
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        User currentUser = currentUserProvider.getCurrentUser();
        validateOwnerOrAdmin(reservation, currentUser, "No tienes autorización para borrar esta reserva");

        reservationRepository.delete(reservation);

        String subject = "Cancelación de tu reserva en Revolux Burger";

        String text = "Estimado/a " + reservation.getName() + ",\n\n" +
                "Te informamos que tu reserva en Revolux Burger ha sido cancelada.\n\n" +
                "Detalles de la reserva cancelada:\n\n" +
                "📅 Fecha: " + reservation.getDate().toLocalDate() + "\n" +
                "⏰ Hora: " + reservation.getDate().toLocalTime() + "\n" +
                "👤 Nombre de la reserva: " + reservation.getName() + "\n" +
                "📧 Correo de contacto: " + reservation.getEmail() + "\n\n" +
                "Si necesitas realizar una nueva reserva, estaremos encantados de atenderte.\n\n" +
                "Para cualquier consulta, no dudes en contactarnos respondiendo a este correo o escribiéndonos directamente a revoluxburger@gmail.com.\n\n" +
                "Gracias por tu comprensión.\n\n" +
                "Atentamente,\n\n" +
                "Equipo Revolux Burger 🍔\n";

        emailService.sendEmail(reservation.getEmail(), subject, text);
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

        // Limitar a 25 personas por bloque de 30 minutos
        LocalDateTime startOfBlock = reservationDateTime.withMinute(minute).withSecond(0).withNano(0);
        LocalDateTime endOfBlock = startOfBlock.plusMinutes(30);

        int existingPersons = reservationRepository.sumNumberOfPersonsByDateBetween(startOfBlock, endOfBlock.minusNanos(1));
        if (existingPersons + reservation.getNumberOfPersons() > 25) {
            throw new RuntimeException("El número máximo de personas (25) ya ha sido alcanzado para este intervalo. Por favor elige otro horario.");
        }
    }

    private ReservationRequest mapToReservationRequest(Reservation reservation) {
        return new ReservationRequest(
                reservation.getId(),
                reservation.getName(),
                reservation.getDescription(),
                reservation.getPhone(),
                reservation.getDate(),
                reservation.getNumberOfPersons(),
                reservation.getEmail(),
                reservation.getUser() != null ? reservation.getUser().getId() : null
        );
    }
}
