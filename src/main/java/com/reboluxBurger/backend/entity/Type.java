package com.reboluxBurger.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
