package com.reboluxBurger.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MenuRequest {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Long typeId;
    private Long points;
    private String imageUrl;
    private BigDecimal price;
}
