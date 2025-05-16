package com.reboluxBurger.backend.controllers;

import com.reboluxBurger.backend.dto.ReservationRequest;
import com.reboluxBurger.backend.entity.Reservation;
import com.reboluxBurger.backend.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimes(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> availableTimes = reservationService.getAvailableTimes(date);
        return ResponseEntity.ok(availableTimes);
    }

    @GetMapping
    public List<ReservationRequest> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }

}
