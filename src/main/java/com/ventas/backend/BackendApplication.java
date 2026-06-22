package com.ventas.backend;

import com.ventas.backend.model.Usuario;
import com.ventas.backend.repository.UsuarioRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initDefaultAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String correoAdmin = "admin@admin.com";
			if (usuarioRepository.existsByCorreo(correoAdmin)) {
				return;
			}

			Usuario admin = new Usuario();
			admin.setNombre("Administrador");
			admin.setCorreo(correoAdmin);
			admin.setContrasena(passwordEncoder.encode("12345"));
			admin.setRol(Usuario.Rol.ADMIN);
			usuarioRepository.save(admin);
		};
	}
}