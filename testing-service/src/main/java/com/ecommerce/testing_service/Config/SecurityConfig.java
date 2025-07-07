package com.ecommerce.testing_service.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.testing_service.Middleware.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // return http
    // .csrf(csrf -> csrf.disable())
    // .authorizeHttpRequests(auth -> auth
    // .requestMatchers("/testing/public/**").permitAll() // kalau kamu punya
    // endpoint public
    // .anyRequest().authenticated())
    // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    // .build();
    // }
    @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // return http
    // .csrf(csrf -> csrf.disable())
    // .sessionManagement(session ->
    // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // // .authorizeHttpRequests(auth -> auth
    // // .requestMatchers("/testing/public/**", "/swagger-ui/**",
    // // "/v3/api-docs/**").permitAll()
    // // .anyRequest().authenticated())
    // .authorizeHttpRequests(auth -> auth
    // .requestMatchers("/auth/**", "/testing/public/**", "/swagger-ui/**",
    // "/v3/api-docs/**")
    // .permitAll()

    // // USER bisa GET dan POST /testing
    // .requestMatchers(HttpMethod.GET, "/testing").hasRole("USER")
    // .requestMatchers(HttpMethod.POST, "/testing").hasRole("USER")

    // // USER bisa akses /testing/my
    // .requestMatchers("/testing/my").hasRole("USER")

    // // ADMIN & KASIR akses semua /testing/**
    // .requestMatchers("/testing/**").hasAnyRole("ADMIN", "KASIR")

    // // Selain itu ditolak
    // .anyRequest().denyAll())
    // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    // .build();
    // }
    // }

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/testing/public/**", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated()) // ⬅ hanya cek token valid
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }
}