package com.ventas.backend.controller;

import com.ventas.backend.repository.DespachoRepository;
import com.ventas.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private DespachoRepository despachoRepo;

    @GetMapping("/pedidos-por-mes")
    public List<Map<String, Object>> pedidosPorMes() {
        return pedidoRepo.pedidosPorMes().stream()
                .map(row -> Map.of("mes", row[0], "total", row[1]))
                .collect(Collectors.toList());
    }

    @GetMapping("/despachos-por-mes")
    public List<Map<String, Object>> despachosPorMes() {
        return despachoRepo.despachosPorMes().stream()
                .map(row -> Map.of("mes", row[0], "total", row[1]))
                .collect(Collectors.toList());
    }
}