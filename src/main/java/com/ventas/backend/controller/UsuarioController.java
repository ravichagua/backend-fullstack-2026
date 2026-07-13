package com.ventas.backend.controller;

import com.ventas.backend.model.Usuario;
import com.ventas.backend.repository.DespachoRepository;
import com.ventas.backend.repository.PedidoRepository;
import com.ventas.backend.repository.UsuarioRepository;
import com.ventas.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private DespachoRepository despachoRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return usuarioRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        if (usuarioRepo.existsByCorreo(body.get("correo"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        Usuario u = new Usuario();
        u.setNombre(body.get("nombre"));
        u.setCorreo(body.get("correo"));
        u.setContrasena(passwordEncoder.encode(body.get("contrasena")));
        u.setRol(Usuario.Rol.valueOf(body.get("rol")));
        return ResponseEntity.ok(usuarioRepo.save(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return usuarioRepo.findById(id).map(u -> {
            u.setNombre(body.get("nombre"));
            u.setCorreo(body.get("correo"));
            u.setRol(Usuario.Rol.valueOf(body.get("rol")));
            String nueva = body.get("contrasena");
            if (nueva != null && !nueva.isBlank()) {
                u.setContrasena(passwordEncoder.encode(nueva));
            }
            return ResponseEntity.ok(usuarioRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String correoActual = jwtUtil.extractCorreo(authHeader.replace("Bearer ", ""));
        return usuarioRepo.findById(id).map(u -> {
            if (u.getCorreo().equals(correoActual)) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puede eliminar su propia cuenta"));
            }
            if (pedidoRepo.existsByUsuarioId(id) || despachoRepo.existsByUsuarioId(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Este usuario tiene operaciones registradas"));
            }
            usuarioRepo.delete(u);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }
}