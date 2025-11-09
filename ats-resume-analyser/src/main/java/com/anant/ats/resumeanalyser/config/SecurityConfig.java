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
                // Allow all requests to the home page ("/") and the form action ("/analyze")
                .requestMatchers("/", "/analyze").permitAll() 
                // All other future pages will be protected
                .anyRequest().authenticated() 
            )
            .httpBasic(withDefaults())
            .csrf(csrf -> csrf
                // Disable CSRF protection for the /analyze endpoint
                .ignoringRequestMatchers("/analyze") 
            ); 

        return http.build();
    }
    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //     http
    //         .authorizeHttpRequests(auth -> auth
    //             // Add "/listmodels" to the list of permitted URLs
    //             .requestMatchers("/", "/analyze", "/listmodels").permitAll() 
    //             .anyRequest().authenticated()
    //         )
    //         .httpBasic(withDefaults())
    //         .csrf(csrf -> csrf.disable()); 

    //     return http.build();
    // }
}