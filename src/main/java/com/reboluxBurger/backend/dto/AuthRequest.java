package com.reboluxBurger.backend.dto;

import com.reboluxBurger.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthRequest {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Long points;
    private Role role;
    private List<ReservationRequest> reservations;
}
