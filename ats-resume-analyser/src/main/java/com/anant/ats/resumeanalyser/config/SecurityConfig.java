package com.anant.ats.resumeanalyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow public access to all our key pages
                .requestMatchers(
                    "/", 
                    "/analyze", 
                    "/register", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**"
                ).permitAll() 
                // Allow GET requests to /login (to see the page)
                .requestMatchers(HttpMethod.GET, "/login").permitAll() 
                // Allow POST requests to /login (to submit the form)
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .anyRequest().authenticated() // All other pages require login
            )
            .formLogin(form -> form
                .loginPage("/login") // This is the URL of our login page
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/") // Redirect to homepage on logout
                .permitAll()
            )
            .csrf(withDefaults()); // Use CSRF protection

        return http.build();
    }
}