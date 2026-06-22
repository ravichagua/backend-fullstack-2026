package com.ventas.backend.controller;

import com.ventas.backend.model.Categoria;
import com.ventas.backend.model.Producto;
import com.ventas.backend.repository.CategoriaRepository;
import com.ventas.backend.repository.DetallePedidoRepository;
import com.ventas.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private CategoriaRepository categoriaRepo;
    @Autowired
    private DetallePedidoRepository detalleRepo;

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) Long cat,
            @RequestParam(required = false) Integer btu) {
        if (cat != null && btu != null)
            return productoRepo.findByCategoriaIdAndBtu(cat, btu);
        if (cat != null)
            return productoRepo.findByCategoriaId(cat);
        if (btu != null)
            return productoRepo.findByBtu(btu);
        return productoRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return productoRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        Categoria cat = categoriaRepo.findById(toLong(body.get("idCategoria"))).orElse(null);
        if (cat == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Seleccione una categoría válida"));
        return ResponseEntity.ok(productoRepo.save(build(new Producto(), body, cat)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return productoRepo.findById(id).map(p -> {
            Categoria cat = categoriaRepo.findById(toLong(body.get("idCategoria"))).orElse(null);
            if (cat == null)
                return ResponseEntity.badRequest().body(Map.of("error", "Seleccione una categoría válida"));
            return ResponseEntity.ok(productoRepo.save(build(p, body, cat)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        return productoRepo.findById(id).map(p -> {
            if (detalleRepo.existsByProductoId(id))
                return ResponseEntity.badRequest().body(Map.of("error", "Este producto tiene pedidos registrados"));
            if (p.getStock() > 0)
                return ResponseEntity.badRequest().body(Map.of("error", "El producto aún tiene unidades en stock"));
            productoRepo.delete(p);
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Producto build(Producto p, Map<String, Object> b, Categoria cat) {
        p.setCategoria(cat);
        p.setNombre((String) b.get("nombre"));
        p.setMarca((String) b.get("marca"));
        p.setModelo((String) b.get("modelo"));
        p.setBtu(Integer.parseInt(b.get("btu").toString()));
        p.setEficiencia((String) b.get("eficiencia"));
        p.setPrecio(new BigDecimal(b.get("precio").toString()));
        p.setStock(Integer.parseInt(b.get("stock").toString()));
        p.setDescripcion(b.getOrDefault("descripcion", "").toString());
        return p;
    }

    private Long toLong(Object o) {
        return o == null ? null : Long.parseLong(o.toString());
    }
}