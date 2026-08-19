package com.depuramente.auth.config;


import com.depuramente.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TYPE = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);

        if (header != null && header.startsWith(AUTH_TYPE)) {
            String token = header.substring(7);

            //TODO: check why token has a white space
            if(token.contains(" ")) {
                logger.info(token);
            }

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                Set<SimpleGrantedAuthority> authorities =
                        jwtUtil.extractRoles(token).stream()
                                .map(role -> new SimpleGrantedAuthority(role.name()))
                                .collect(Collectors.toSet());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}