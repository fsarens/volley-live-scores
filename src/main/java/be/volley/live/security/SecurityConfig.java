package be.volley.live.security;

import be.volley.live.repository.DashboardTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppOAuth2UserService appOAuth2UserService;
    private final DashboardTokenRepository dashboardTokenRepository;

    public SecurityConfig(AppOAuth2UserService appOAuth2UserService,
                          DashboardTokenRepository dashboardTokenRepository) {
        this.appOAuth2UserService = appOAuth2UserService;
        this.dashboardTokenRepository = dashboardTokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new DashboardTokenFilter(dashboardTokenRepository),
                    OAuth2AuthorizationRequestRedirectFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/score", "/score/**", "/scorer", "/scorer/**").hasAnyRole("ADMIN", "SCORER")
                .requestMatchers("/dashboard", "/dashboard/**", "/api", "/api/**").hasAnyRole("ADMIN", "SCORER", "DASHBOARD")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(u -> u.oidcUserService(appOAuth2UserService))
                .defaultSuccessUrl("/score", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
            );

        return http.build();
    }
}
