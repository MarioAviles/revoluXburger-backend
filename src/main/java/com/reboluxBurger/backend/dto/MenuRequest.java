package com.reboluxBurger.backend.dto;

import com.reboluxBurger.backend.enums.Category;
import com.reboluxBurger.backend.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MenuRequest {
    private Long id;
    private String name;
    private String description;
    private Category category;
    private Type type;
    private Long points;
    private String imageUrl;
    private BigDecimal price;
}
