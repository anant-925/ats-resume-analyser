package com.anant.ats.resumeanalyser.config;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow all requests to the home page ("/") and the form action ("/analyze")
                .requestMatchers("/", "/analyze").permitAll() 
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            // THIS IS THE CHANGE:
            // We are no longer disabling CSRF. We'll use the default, secure settings.
            .csrf(withDefaults());

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