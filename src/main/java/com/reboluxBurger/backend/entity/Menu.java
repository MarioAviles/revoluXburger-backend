package com.reboluxBurger.backend.entity;

import com.reboluxBurger.backend.enums.Category;
import com.reboluxBurger.backend.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column (nullable = false)
    private Long points;

    @Column (nullable = false)
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;


    public Menu(Long id, String name, String description, Category category, BigDecimal price, Long points, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.points = points;
        this.imageUrl = imageUrl;
        this.type = null;
    }

    public Menu(Long id, String name, String description, Category category, Type type, BigDecimal price, Long points, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
        this.price = price;
        this.points = points;
        this.imageUrl = imageUrl;
    }



}
