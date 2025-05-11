package com.reboluxBurger.backend.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Lógica para cargar el usuario desde la base de datos
        return new org.springframework.security.core.userdetails.User(
                username,
                "contraseñaEncriptada",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
