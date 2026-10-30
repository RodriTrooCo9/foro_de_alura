package com.gep.foro_alura.infra.security;

import com.gep.foro_alura.domain.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");

        if (authHeader != null){
            if (!authHeader.startsWith("Bearer ")) {
                response.sendError(401, "Invalid authorization header");
                return;
            }
            try {
            var token = authHeader.replace("Bearer ", "");
            var username = tokenService.getSubject(token);

            if (username != null){
                var usuario = usuarioRepository.findByUsername(username);
                if (usuario == null || !usuario.isEnabled()) {
                    response.sendError(401, "Invalid user");
                    return;
                }
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
                SecurityContextHolder.clearContext();
                response.sendError(401, "Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}



