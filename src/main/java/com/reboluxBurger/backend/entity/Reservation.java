package com.reboluxBurger.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    private String description;

    @Column(nullable = false)
    @Pattern(regexp = "^[6-7-9]\\d{8}$", message = "El número de teléfono debe ser español y contener exactamente 9 dígitos, comenzando con 6, 7 o 9.")
    private String phone;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference
    private User user;

}
