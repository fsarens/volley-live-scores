package be.volley.live.security;

import be.volley.live.model.DashboardToken;
import be.volley.live.repository.DashboardTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Validates ?token= query parameter for dashboard and API routes.
 * If valid, sets a ROLE_DASHBOARD authentication in the security context.
 */
public class DashboardTokenFilter extends OncePerRequestFilter {

    private final DashboardTokenRepository dashboardTokenRepository;

    public DashboardTokenFilter(DashboardTokenRepository dashboardTokenRepository) {
        this.dashboardTokenRepository = dashboardTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // If already authenticated (e.g. from session), skip token check
        if (SecurityContextHolder.getContext().getAuthentication() == null
                || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

            String token = request.getParameter("token");
            if (token != null) {
                Optional<DashboardToken> dashboardToken = dashboardTokenRepository.findByToken(token);
                if (dashboardToken.isPresent()) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            "dashboard:" + dashboardToken.get().getTenantId(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_DASHBOARD"))
                    );
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);
                    // Save to session so subsequent API calls don't need the token
                    new HttpSessionSecurityContextRepository()
                            .saveContext(context, request, response);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
