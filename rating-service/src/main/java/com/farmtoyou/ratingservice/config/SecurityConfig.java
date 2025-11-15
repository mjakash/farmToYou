package com.farmtoyou.ratingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * This configuration disables Spring Security's default authentication.
     * We trust that all requests coming to this service have already been
     * authenticated by the API Gateway, which has passed the user's
     * ID and Role in the 'X-User-Id' and 'X-User-Role' headers.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .authorizeHttpRequests(authz -> authz
                // Permit all requests to enter the service
                .requestMatchers("/api/ratings/**").permitAll() 
                .anyRequest().authenticated()
            );
        return http.build();
    }
}