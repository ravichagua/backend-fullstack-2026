package com.ventas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "despacho")
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties("despacho")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.LISTO;

    @Column(name = "fecha_despacho")
    private LocalDate fechaDespacho;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "imagen_comprobante", length = 255)
    private String imagenComprobante;

    @Column(length = 300)
    private String observacion;

    public enum Estado {
        LISTO, EN_CAMINO, ENTREGADO
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario u) {
        this.usuario = u;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion d) {
        this.direccion = d;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(LocalDate f) {
        this.fechaDespacho = f;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate f) {
        this.fechaEntrega = f;
    }

    public String getImagenComprobante() {
        return imagenComprobante;
    }

    public void setImagenComprobante(String s) {
        this.imagenComprobante = s;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String o) {
        this.observacion = o;
    }
}