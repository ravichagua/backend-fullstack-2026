package com.ventas.backend.repository;

import com.ventas.backend.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {
  Optional<Despacho> findByPedidoId(Long pedidoId);

  boolean existsByUsuarioId(Long usuarioId);

  boolean existsByDireccionIdAndEstadoNot(Long direccionId, Despacho.Estado estado);

  @Query(value = """
      SELECT MONTHNAME(fecha_entrega) as mes, COUNT(*) as total
      FROM despacho
      WHERE estado = 'ENTREGADO'
        AND fecha_entrega >= DATE_SUB(CURDATE(), INTERVAL 10 MONTH)
      GROUP BY MONTH(fecha_entrega), MONTHNAME(fecha_entrega)
      ORDER BY MONTH(fecha_entrega)
      """, nativeQuery = true)
  List<Object[]> despachosPorMes();
}