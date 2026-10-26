package com.pedidos670.pedidos.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    // Sin caracteres ambiguos (0/O, 1/I) para que el código sea fácil de leer y escribir
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("clienteId")
    @JsonAlias({"clienteID", "cliente_id"})
    @Column(name = "CLIENTE_ID", nullable = false)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private OrderStatus estado;

    @Column(name = "FECHA_CREACION")
    private LocalDateTime fechaCreacion;

    @Column(name = "TOTAL")
    private Double total;

    // Código público para seguir el pedido sin iniciar sesión (ej: P360-AB12CD).
    // Nullable porque los pedidos antiguos no lo tienen.
    @Column(name = "CODIGO_SEGUIMIENTO", unique = true, length = 20)
    private String codigoSeguimiento;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ORDER_ID")
    private List<OrderItem> items;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.codigoSeguimiento == null) {
            this.codigoSeguimiento = generarCodigo();
        }
    }

    private static String generarCodigo() {
        StringBuilder sb = new StringBuilder("P360-");
        for (int i = 0; i < 6; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}