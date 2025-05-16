package com.reboluxBurger.backend.repository;

import com.reboluxBurger.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByDateBefore(LocalDateTime dateTime);
    int countByDateBetween(LocalDateTime start, LocalDateTime end);

}
