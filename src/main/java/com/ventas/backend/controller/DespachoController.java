package com.ventas.backend.controller;

import com.ventas.backend.model.Despacho;
import com.ventas.backend.model.Pedido;
import com.ventas.backend.repository.DespachoRepository;
import com.ventas.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    @Autowired
    private DespachoRepository despachoRepo;
    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${upload.dir}")
    private String uploadDir;

    @GetMapping
    public List<Despacho> listar() {
        return despachoRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Despacho> obtener(@PathVariable Long id) {
        return despachoRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return despachoRepo.findById(id).map(d -> {
            if (d.getEstado() == Despacho.Estado.ENTREGADO)
                return ResponseEntity.badRequest().body(Map.of("error", "El estado no puede retroceder"));

            Despacho.Estado nuevo = Despacho.Estado.valueOf(body.get("estado"));
            String obs = body.getOrDefault("observacion", "");

            d.setEstado(nuevo);
            if (!obs.isBlank())
                d.setObservacion(obs);

            if (nuevo == Despacho.Estado.EN_CAMINO)
                d.setFechaDespacho(LocalDate.now());

            if (nuevo == Despacho.Estado.ENTREGADO) {
                d.setFechaEntrega(LocalDate.now());
                Pedido p = d.getPedido();
                p.setEstado(Pedido.Estado.COMPLETADO);
                p.setFechaCierre(LocalDate.now());
                pedidoRepo.save(p);
            }

            Despacho saved = despachoRepo.save(d);

            messagingTemplate.convertAndSend("/topic/despachos", Map.of(
                    "id", saved.getId(),
                    "idPedido", saved.getPedido().getId(),
                    "estado", saved.getEstado().name()));

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(@PathVariable Long id,
            @RequestParam("imagen") MultipartFile file) {
        return despachoRepo.findById(id).map(d -> {
            if (d.getEstado() != Despacho.Estado.ENTREGADO)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Solo se puede subir imagen cuando el estado es ENTREGADO"));
            try {
                Path dir = Paths.get(uploadDir);
                Files.createDirectories(dir);
                String ext = "";
                String original = file.getOriginalFilename();
                if (original != null && original.contains("."))
                    ext = original.substring(original.lastIndexOf("."));
                String filename = "despacho_" + id + "_" + System.currentTimeMillis() + ext;
                Files.copy(file.getInputStream(), dir.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
                d.setImagenComprobante("/uploads/" + filename);
                return ResponseEntity.ok(despachoRepo.save(d));
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Error al subir imagen"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}