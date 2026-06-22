package com.ventas.backend.controller;

import com.ventas.backend.model.Cliente;
import com.ventas.backend.model.Despacho;
import com.ventas.backend.model.Direccion;
import com.ventas.backend.repository.ClienteRepository;
import com.ventas.backend.repository.DespachoRepository;
import com.ventas.backend.repository.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class DireccionController {

    @Autowired
    private DireccionRepository direccionRepo;
    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired
    private DespachoRepository despachoRepo;

    @GetMapping("/api/clientes/{idCliente}/direcciones")
    public ResponseEntity<?> listar(@PathVariable Long idCliente) {
        if (!clienteRepo.existsById(idCliente))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(direccionRepo.findByClienteId(idCliente));
    }

    @PostMapping("/api/clientes/{idCliente}/direcciones")
    public ResponseEntity<?> agregar(@PathVariable Long idCliente,
            @RequestBody Map<String, String> body) {
        Cliente cliente = clienteRepo.findById(idCliente).orElse(null);
        if (cliente == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Cliente no encontrado"));
        Direccion d = new Direccion();
        d.setCliente(cliente);
        d.setReferencia(body.get("referencia"));
        return ResponseEntity.ok(direccionRepo.save(d));
    }

    @DeleteMapping("/api/direcciones/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (despachoRepo.existsByDireccionIdAndEstadoNot(id, Despacho.Estado.ENTREGADO))
            return ResponseEntity.badRequest().body(Map.of("error", "Esta dirección tiene un despacho en curso"));
        direccionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Dirección eliminada"));
    }
}