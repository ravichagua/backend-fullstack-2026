package com.ventas.backend.repository;

import com.ventas.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByUsuarioId(Long usuarioId);

    @Query(value = """
            SELECT MONTHNAME(fecha) as mes, SUM(total) as total
            FROM pedido
            WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 10 MONTH)
            GROUP BY MONTH(fecha), MONTHNAME(fecha)
            ORDER BY MONTH(fecha)
            """, nativeQuery = true)
    List<Object[]> pedidosPorMes();
}