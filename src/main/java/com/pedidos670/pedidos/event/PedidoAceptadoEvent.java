package com.pedidos670.pedidos.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAceptadoEvent {

    private UUID eventId;

    private String eventType;

    private Integer version;

    private Long pedidoId;

    private Long productoId;

    private Integer cantidad;

    private LocalDateTime occurredAt;
}