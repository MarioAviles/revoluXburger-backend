package com.reboluxBurger.backend.repository;

import com.reboluxBurger.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByDateBefore(LocalDateTime dateTime);
    int countByDateBetween(LocalDateTime start, LocalDateTime end);

    //para obtener las reservas agrupadas por hora
    @Query("SELECT FUNCTION('DATE_FORMAT', r.date, '%Y-%m-%dT%H:%i') as timeSlot, COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.date BETWEEN :start AND :end " +
            "GROUP BY timeSlot")
    List<Object[]> countReservationsGroupedByTimeSlot(LocalDateTime start, LocalDateTime end);

}
