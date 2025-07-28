package com.example.tasksapi.auth;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String userEmail = null;

        try{
            userEmail = jwtService.extractEmail(jwt);
        }catch (io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.security.SignatureException e){
            //Essas exceptions ja estao sendo tratadas no entrypoint, por isso a ideia aqui é passar a request adiante(neste caso sem autenticar), mas não lancar a exception para nao poluir a console.
            request.setAttribute("jwt_exception", e);
            filterChain.doFilter(request, response);
            return;
        }

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userService.findByEmail(userEmail).orElse(null);

            if(user != null && jwtService.isTokenValid(jwt, user)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, null);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
