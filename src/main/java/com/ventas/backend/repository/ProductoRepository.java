package com.ventas.backend.repository;

import com.ventas.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByBtu(Integer btu);

    List<Producto> findByCategoriaIdAndBtu(Long categoriaId, Integer btu);

    boolean existsByCategoriaId(Long categoriaId);
}