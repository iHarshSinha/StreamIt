package com.streamit.groupchatapp.security.jwt;

import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Read Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2️⃣ If no header or not Bearer -> skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Extract token
        String token = authHeader.substring(7);

        // 4️⃣ Validate token
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5️⃣ Extract user identity
        String email = jwtService.extractEmail(token);

        // 6️⃣ Avoid re-authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // 7️⃣ Load user from DB
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 8️⃣ Create authorities
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

            // 9️⃣ Create Authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 🔟 Set authentication into SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 1️⃣1️⃣ Continue filter chain
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/")
                || request.getServletPath().startsWith("/oauth2/")
                || request.getServletPath().startsWith("/login/");
    }
}