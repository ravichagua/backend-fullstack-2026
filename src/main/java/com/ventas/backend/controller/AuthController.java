package com.ventas.backend.controller;

import com.ventas.backend.dto.LoginRequest;
import com.ventas.backend.repository.UsuarioRepository;
import com.ventas.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return usuarioRepo.findByCorreo(request.getCorreo())
                .filter(u -> passwordEncoder.matches(request.getContrasena(), u.getContrasena()))
                .map(u -> ResponseEntity.ok(Map.of(
                        "token", jwtUtil.generateToken(u.getCorreo(), u.getRol().name()),
                        "nombre", u.getNombre(),
                        "rol", u.getRol().name())))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas")));
    }
}