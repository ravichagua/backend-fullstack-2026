package com.ventas.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventas.backend.model.*;
import com.ventas.backend.repository.*;
import com.ventas.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private DireccionRepository direccionRepo;
    @Autowired
    private DespachoRepository despachoRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Long idCliente) {
        if (idCliente != null)
            return ResponseEntity.ok(pedidoRepo.findByClienteId(idCliente));
        return ResponseEntity.ok(pedidoRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return pedidoRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario vendedor = usuarioRepo.findByCorreo(correo).orElse(null);
        if (vendedor == null)
            return ResponseEntity.status(401).build();

        Cliente cliente = clienteRepo.findById(toLong(body.get("idCliente"))).orElse(null);
        if (cliente == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Seleccione un cliente válido"));

        List<Map<String, Object>> items;
        try {
            items = mapper.readValue(body.get("items").toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Items inválidos"));
        }
        if (items == null || items.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Agregue al menos un producto al pedido"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUsuario(vendedor);
        pedido.setMetodoPago(Pedido.MetodoPago.valueOf((String) body.get("metodoPago")));
        pedido.setTipoPedido(Pedido.TipoPedido.valueOf((String) body.get("tipoPedido")));
        pedido.setEstado(Pedido.Estado.PENDIENTE);
        pedido.setFecha(LocalDate.now());

        List<DetallePedido> detalle = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> item : items) {
            Producto prod = productoRepo.findById(toLong(item.get("idProducto"))).orElse(null);
            if (prod == null)
                return ResponseEntity.badRequest().body(Map.of("error", "Producto no encontrado"));
            int cantidad = Integer.parseInt(item.get("cantidad").toString());
            if (prod.getStock() < cantidad)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Stock insuficiente para " + prod.getNombre()));

            DetallePedido dp = new DetallePedido();
            dp.setProducto(prod);
            dp.setCantidad(cantidad);
            dp.setPrecioUnit(prod.getPrecio());
            dp.setSubtotal(prod.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
            dp.setPedido(pedido);
            detalle.add(dp);
            total = total.add(dp.getSubtotal());

            prod.setStock(prod.getStock() - cantidad);
            productoRepo.save(prod);
        }

        pedido.setTotal(total);
        pedido.setDetalle(detalle);
        Pedido saved = pedidoRepo.save(pedido);

        if (pedido.getTipoPedido() == Pedido.TipoPedido.DOMICILIO) {
            Object idDirObj = body.get("idDireccion");
            if (idDirObj == null || idDirObj.toString().isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Seleccione una dirección de entrega"));
            Direccion dir = direccionRepo.findById(toLong(idDirObj)).orElse(null);
            if (dir == null)
                return ResponseEntity.badRequest().body(Map.of("error", "Dirección no encontrada"));

            Despacho despacho = new Despacho();
            despacho.setPedido(saved);
            despacho.setDireccion(dir);
            despacho.setUsuario(vendedor);
            despacho.setEstado(Despacho.Estado.LISTO);
            despachoRepo.save(despacho);
        }

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        return pedidoRepo.findById(id).map(pedido -> {
            if (pedido.getEstado() != Pedido.Estado.PENDIENTE)
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se pueden cancelar pedidos PENDIENTES"));
            despachoRepo.findByPedidoId(id).ifPresent(d -> {
                if (d.getEstado() == Despacho.Estado.EN_CAMINO || d.getEstado() == Despacho.Estado.ENTREGADO)
                    throw new RuntimeException("No se puede cancelar: el pedido ya salió para entrega");
            });
            if (pedido.getDetalle() != null) {
                for (DetallePedido dp : pedido.getDetalle()) {
                    Producto prod = dp.getProducto();
                    prod.setStock(prod.getStock() + dp.getCantidad());
                    productoRepo.save(prod);
                }
            }
            pedido.setEstado(Pedido.Estado.CANCELADO);
            pedido.setFechaCierre(LocalDate.now());
            return ResponseEntity.ok(pedidoRepo.save(pedido));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<?> completar(@PathVariable Long id) {
        return pedidoRepo.findById(id).map(pedido -> {
            if (pedido.getTipoPedido() != Pedido.TipoPedido.RECOJO)
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se completan manualmente los pedidos RECOJO"));
            pedido.setEstado(Pedido.Estado.COMPLETADO);
            pedido.setFechaCierre(LocalDate.now());
            return ResponseEntity.ok(pedidoRepo.save(pedido));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Long toLong(Object o) {
        return o == null ? null : Long.parseLong(o.toString());
    }
}