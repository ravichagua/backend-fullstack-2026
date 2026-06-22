package com.ventas.backend.controller;

import com.ventas.backend.model.Cliente;
import com.ventas.backend.repository.ClienteRepository;
import com.ventas.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired
    private PedidoRepository pedidoRepo;

    @GetMapping
    public List<Cliente> listar() {
        return clienteRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        return clienteRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        if (clienteRepo.existsByDni(body.get("dni")))
            return ResponseEntity.badRequest().body(Map.of("error", "El DNI ya está registrado"));
        Cliente c = new Cliente();
        c.setNombre(body.get("nombre"));
        c.setDni(body.get("dni"));
        c.setTelefono(body.getOrDefault("telefono", ""));
        return ResponseEntity.ok(clienteRepo.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return clienteRepo.findById(id).map(c -> {
            c.setNombre(body.get("nombre"));
            c.setDni(body.get("dni"));
            c.setTelefono(body.getOrDefault("telefono", ""));
            return ResponseEntity.ok(clienteRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (pedidoRepo.existsByClienteId(id))
            return ResponseEntity.badRequest().body(Map.of("error", "Este cliente tiene pedidos registrados"));
        clienteRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado"));
    }
}