package com.ventas.backend.config;

import com.ventas.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/uploads/**").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers("/api/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/api/productos/**").hasRole("VENDEDOR")
                        .requestMatchers("/api/clientes/**").hasRole("VENDEDOR")
                        .requestMatchers("/api/pedidos/**").hasRole("VENDEDOR")
                        .requestMatchers("/api/direcciones/**").hasRole("VENDEDOR")
                        .requestMatchers("/api/despachos/**").hasRole("DESPACHADOR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}