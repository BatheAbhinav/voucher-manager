package abhinav.projects.vouchermanager.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
class AuthenticationFilter extends OncePerRequestFilter {

    static final String ATTRIBUTE = "authenticatedUser";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                request.setAttribute(ATTRIBUTE, jwtService.verify(token));
            } catch (JwtException | IllegalArgumentException e) {
                // Invalid or expired token: leave the request unauthenticated.
                // Downstream authorization checks (CurrentPrincipal) reject as needed.
            }
        }
        chain.doFilter(request, response);
    }
}
