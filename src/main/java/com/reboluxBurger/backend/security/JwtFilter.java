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

    // Constructor con @Lazy para evitar problemas de dependencias circulares con UserDetailsService
    public JwtFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Este método permite excluir rutas públicas o preflight requests del filtro JWT
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Permitir solicitudes CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // Excluir rutas que no requieren autenticación JWT (auth, docs, etc.)
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/forgot-password")
                || path.startsWith("/auth/reset-password")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    // Filtro principal: valida el JWT, autentica al usuario si el token es válido
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay encabezado Authorization o no empieza con "Bearer ", no se procesa
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Extraer el token JWT (sin el prefijo "Bearer ")
        String token = authHeader.substring(7);
        String username = null;

        try {
            // Extraer el nombre de usuario del token
            username = jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException e) {
            // Si el token está expirado, continuar sin autenticar
            chain.doFilter(request, response);
            return;
        } catch (Exception e) {
            // Si el token es inválido, continuar sin autenticar
            chain.doFilter(request, response);
            return;
        }

        // Si el usuario fue extraído y no hay una autenticación activa en el contexto de seguridad
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar detalles del usuario desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Crear objeto de autenticación con los roles/permisos del usuario
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Agregar detalles del request actual (IP, sesión, etc.)
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establecer autenticación en el contexto de seguridad para el resto del request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }

}
