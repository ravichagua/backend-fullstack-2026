package com.ventas.backend.controller;

import com.ventas.backend.dto.LoginRequest;
import com.ventas.backend.repository.UsuarioRepository;
import com.ventas.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login intento: correo={}", request.getCorreo());

        return usuarioRepo.findByCorreo(request.getCorreo())
                .filter(u -> passwordEncoder.matches(request.getContrasena(), u.getContrasena()))
                .map(u -> {
                    log.info("Login OK: correo={} rol={}", u.getCorreo(), u.getRol());
                    return ResponseEntity.ok(Map.of(
                            "token", jwtUtil.generateToken(u.getCorreo(), u.getRol().name()),
                            "nombre", u.getNombre(),
                            "rol", u.getRol().name()));
                })
                .orElseGet(() -> {
                    log.warn("Login FALLIDO: correo={}", request.getCorreo());
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
                });
    }
}