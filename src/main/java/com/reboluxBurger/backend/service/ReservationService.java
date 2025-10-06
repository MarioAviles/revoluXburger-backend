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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EmailService emailService;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            EmailService emailService
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.emailService = emailService;
    }

    /**
     * Elimina reservas expiradas diariamente a las 03:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredReservations() {
        reservationRepository.deleteExpiredReservations();
    }

    /**
     * Devuelve todas las reservas:
     * - Si el usuario es admin, obtiene todas.
     * - Si no, solo las reservas del usuario autenticado.
     */
    public List<ReservationRequest> getAllReservations() {
        User currentUser = currentUserProvider.getCurrentUser();

        List<Reservation> reservations = currentUser.getRole() == Role.ADMIN
                ? reservationRepository.findAll()
                : reservationRepository.findByUserId(currentUser.getId());

        return reservations.stream()
                .map(this::mapToReservationRequest)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva reserva. Si el usuario no está autenticado, la asocia al usuario "anonymous".
     * También envía un correo de confirmación y genera enlace para Google Calendar.
     */
    public Reservation createReservation(Reservation reservation) {
        validateReservationFields(reservation);

        User currentUser = currentUserProvider.getCurrentUser();
        reservation.setUser(currentUser != null ? currentUser : userRepository.findByUsername("anonymous")
                .orElseThrow(() -> new IllegalStateException("Usuario anónimo no encontrado")));

        Reservation savedReservation = reservationRepository.save(reservation);

        // Generar enlace para Google Calendar
        LocalDateTime start = savedReservation.getDate();
        LocalDateTime end = start.plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String startTime = start.format(formatter);
        String endTime = end.format(formatter);

        String calendarUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + encode("Reserva en Revolux Burger")
                + "&dates=" + startTime + "/" + endTime
                + "&details=" + encode("Reserva en Revolux Burger para " + savedReservation.getName())
                + "&location=" + encode("Revolux Burger, Calle Carlos III")
                + "&sf=true&output=xml";

        // Enviar email de confirmación
        String subject = "Confirmación de tu reserva en Revolux Burger";
        String text = generateConfirmationEmailText(savedReservation, calendarUrl);

     //   try {
      //      emailService.sendEmail(savedReservation.getEmail(), subject, text);
      //  } catch (Exception e) {
        //    System.err.println("Error al enviar el correo: " + e.getMessage());
       // }

        return savedReservation;
    }

    /**
     * Actualiza una reserva existente si el usuario es el dueño o un admin.
     */
    public Reservation updateReservation(Long reservationId, Reservation updatedReservation) {
        Reservation existingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("No existe una reserva con ese id"));

        User currentUser = currentUserProvider.getCurrentUser();
        validateOwnerOrAdmin(existingReservation, currentUser, "No tienes autorización para actualizar esta reserva");

        validateReservationFields(updatedReservation);

        updatedReservation.setUser(existingReservation.getUser());
        existingReservation.setDate(updatedReservation.getDate());
        existingReservation.setDescription(updatedReservation.getDescription());
        existingReservation.setName(updatedReservation.getName());
        existingReservation.setPhone(updatedReservation.getPhone());
        existingReservation.setNumberOfPersons(updatedReservation.getNumberOfPersons());

        return reservationRepository.save(existingReservation);
    }

    /**
     * Elimina una reserva si el usuario es el propietario o un administrador.
     * También envía correo de cancelación.
     */
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        User currentUser = currentUserProvider.getCurrentUser();
        validateOwnerOrAdmin(reservation, currentUser, "No tienes autorización para borrar esta reserva");

        reservationRepository.delete(reservation);

        String subject = "Cancelación de tu reserva en Revolux Burger";
        String text = generateCancellationEmailText(reservation);

        try {
            emailService.sendEmail(reservation.getEmail(), subject, text);
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    /**
     * Devuelve un mapa con los tramos horarios y la cantidad de reservas por fecha específica.
     */
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

    /**
     * Devuelve los horarios disponibles filtrando aquellos que tengan menos de 10 reservas.
     */
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

    // ---------------------- MÉTODOS AUXILIARES ----------------------

    private void validateOwnerOrAdmin(Reservation reservation, User user, String errorMessage) {
        boolean isOwner = reservation.getUser() != null && reservation.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) throw new RuntimeException(errorMessage);
    }

    private void validateReservationFields(Reservation reservation) {
        if (reservation.getPhone() == null || !reservation.getPhone().matches("^[679]\\d{8}$"))
            throw new RuntimeException("El número de teléfono debe tener 9 dígitos y comenzar por 6, 7 o 9");

        if (reservation.getName() == null || reservation.getName().trim().isEmpty())
            throw new RuntimeException("El nombre es obligatorio");

        if (reservation.getDate() == null)
            throw new RuntimeException("La fecha de la reserva es obligatoria");

        if (reservation.getEmail() == null || !reservation.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new RuntimeException("El email no es válido");

        LocalDateTime reservationDateTime = reservation.getDate();
        LocalTime reservationTime = reservationDateTime.toLocalTime();

        if (reservationDateTime.isBefore(LocalDateTime.now()))
            throw new RuntimeException("La reserva no puede ser en el pasado");

        // Validar hora y bloque de 30 minutos
        int minute = reservationTime.getMinute();
        if (minute % 15 != 0)
            throw new RuntimeException("La reserva debe ser en intervalos de 15 minutos (00, 15, 30, 45)");

        boolean isLunch = !reservationTime.isBefore(LocalTime.of(13, 0)) && reservationTime.isBefore(LocalTime.of(16, 0));
        boolean isDinner = !reservationTime.isBefore(LocalTime.of(20, 0)) && reservationTime.isBefore(LocalTime.of(23, 0));
        if (!isLunch && !isDinner)
            throw new RuntimeException("La hora de la reserva debe estar entre las 13:00-16:00 o 20:00-23:00");

        // Validar capacidad por intervalo
        LocalDateTime startOfBlock = reservationDateTime.withMinute(minute).withSecond(0).withNano(0);
        LocalDateTime endOfBlock = startOfBlock.plusMinutes(30);

        int existingPersons = reservationRepository.sumNumberOfPersonsByDateBetween(startOfBlock, endOfBlock.minusNanos(1));
        if (existingPersons + reservation.getNumberOfPersons() > 25)
            throw new RuntimeException("El número máximo de personas (25) ya ha sido alcanzado para este intervalo.");
    }

    private List<LocalTime> generateAllowedTimes() {
        return Stream.of(
                generateTimeRange(LocalTime.of(13, 0), LocalTime.of(16, 0)),
                generateTimeRange(LocalTime.of(20, 0), LocalTime.of(23, 0))
        ).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<LocalTime> generateTimeRange(LocalTime start, LocalTime end) {
        List<LocalTime> result = new java.util.ArrayList<>();
        for (LocalTime time = start; time.isBefore(end); time = time.plusMinutes(30)) {
            result.add(time);
        }
        return result;
    }

    private String generateConfirmationEmailText(Reservation r, String calendarUrl) {
        return "Estimado/a " + r.getName() + ",\n\n" +
                "Te agradecemos por elegir Revolux Burger para tu próxima visita. Tu reserva ha sido registrada exitosamente.\n\n" +
                "📅 Fecha: " + r.getDate().toLocalDate() + "\n" +
                "⏰ Hora: " + r.getDate().toLocalTime() + "\n" +
                "👤 Nombre: " + r.getName() + "\n" +
                "📧 Email: " + r.getEmail() + "\n" +
                "👥 Personas: " + r.getNumberOfPersons() + "\n\n" +
                "🗓 Agrega esta reserva a Google Calendar:\n" + calendarUrl + "\n\n" +
                "Gracias por confiar en nosotros.\n\nEquipo Revolux Burger 🍔";
    }

    private String generateCancellationEmailText(Reservation r) {
        return "Estimado/a " + r.getName() + ",\n\n" +
                "Te informamos que tu reserva ha sido cancelada.\n\n" +
                "📅 Fecha: " + r.getDate().toLocalDate() + "\n" +
                "⏰ Hora: " + r.getDate().toLocalTime() + "\n" +
                "📧 Email: " + r.getEmail() + "\n\n" +
                "Esperamos verte pronto. Gracias por tu comprensión.\n\nEquipo Revolux Burger 🍔";
    }

    private String encode(String text) {
        return java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
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
