package com.ventas.backend.config;

import com.ventas.backend.model.Usuario;
import com.ventas.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.findByCorreo("admin@ventas.com").isEmpty()) {

                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setCorreo("admin@ventas.com");
                admin.setContrasena(passwordEncoder.encode("admin123"));
                admin.setRol(Usuario.Rol.ADMIN);

                usuarioRepository.save(admin);

                System.out.println("Administrador creado correctamente");
            }
        };
    }
}