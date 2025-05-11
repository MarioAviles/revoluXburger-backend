package com.reboluxBurger.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Constructor inyecta dependencias: JwtUtil para manejar tokens y UserDetailsService para cargar detalles del usuario.
    public JwtFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    // Este método excluye ciertas rutas (como Swagger) del filtro para que no sean procesadas.
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    // Método principal que ejecuta la lógica del filtro para autenticar las solicitudes.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Obtiene el encabezado de autorización de la solicitud.
        String authHeader = request.getHeader("Authorization");

        // Si el encabezado es nulo o no comienza con "Bearer ", pasa la solicitud al siguiente filtro sin hacer nada.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Elimina el prefijo "Bearer " para obtener solo el token.
        String token = authHeader.substring(7);
        String username = null;

        try {
            // Intenta extraer el nombre de usuario del token usando JwtUtil.
            username = jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException e) {
            // Si el token ha expirado, responde con un estado 401 (No autorizado) y no continúa.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Si se obtuvo un nombre de usuario y no hay autenticación activa en el contexto de seguridad.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carga los detalles del usuario desde UserDetailsService.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Crea un token de autenticación usando los detalles del usuario.
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Añade detalles adicionales del contexto de solicitud.
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establece la autenticación en el contexto de seguridad de Spring.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Pasa la solicitud al siguiente filtro en la cadena de filtros.
        chain.doFilter(request, response);
    }
}
