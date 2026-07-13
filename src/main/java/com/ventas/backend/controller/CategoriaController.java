package com.ventas.backend.controller;

import com.ventas.backend.model.Categoria;
import com.ventas.backend.repository.CategoriaRepository;
import com.ventas.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepo;
    @Autowired
    private ProductoRepository productoRepo;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return categoriaRepo.findById(id).map(c -> ResponseEntity.ok(Map.of(
                "id", c.getId(),
                "nombre", c.getNombre(),
                "descripcion", c.getDescripcion() != null ? c.getDescripcion() : "",
                "productos", productoRepo.findByCategoriaId(id)))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        if (categoriaRepo.existsByNombreIgnoreCase(body.get("nombre"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una categoría con ese nombre"));
        }
        Categoria c = new Categoria();
        c.setNombre(body.get("nombre"));
        c.setDescripcion(body.get("descripcion"));
        return ResponseEntity.ok(categoriaRepo.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return categoriaRepo.findById(id).map(c -> {
            c.setNombre(body.get("nombre"));
            c.setDescripcion(body.get("descripcion"));
            return ResponseEntity.ok(categoriaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (productoRepo.existsByCategoriaId(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Esta categoría tiene productos asignados"));
        }
        categoriaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Categoría eliminada"));
    }
}