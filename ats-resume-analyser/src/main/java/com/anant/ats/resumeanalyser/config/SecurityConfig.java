package com.anant.ats.resumeanalyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow all requests to endpoints starting with /test
                .requestMatchers("/test/**").permitAll() 
                // All other requests must be authenticated
                .anyRequest().authenticated() 
            )
            .httpBasic(withDefaults()) // Use basic authentication for other routes
            .csrf(csrf -> csrf
                // IMPORTANT: Disable CSRF protection for our test endpoint
                // Otherwise POST requests will still be blocked
                .ignoringRequestMatchers("/test/**") 
            ); 

        return http.build();
    }
}