package com.pedidos670.pedidos.service;

import com.pedidos670.pedidos.event.PedidoAceptadoEvent;
import com.pedidos670.pedidos.messaging.RabbitNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoEventPublisher {

    private static final String EVENT_TYPE_PEDIDO_ACEPTADO =
            "PEDIDO_ACEPTADO";

    private static final int EVENT_VERSION = 1;

    private final RabbitTemplate rabbitTemplate;

    public void publicarPedidoAceptado(
            Long pedidoId,
            Long productoId,
            Integer cantidad
    ) {

        PedidoAceptadoEvent evento =
                PedidoAceptadoEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(EVENT_TYPE_PEDIDO_ACEPTADO)
                        .version(EVENT_VERSION)
                        .pedidoId(pedidoId)
                        .productoId(productoId)
                        .cantidad(cantidad)
                        .occurredAt(LocalDateTime.now())
                        .build();

        rabbitTemplate.convertAndSend(
                RabbitNames.PEDIDOS_EXCHANGE,
                RabbitNames.PEDIDO_ACEPTADO_ROUTING_KEY,
                evento
        );

        log.info(
                "Evento PEDIDO_ACEPTADO enviado a RabbitMQ | eventId={} | pedidoId={} | productoId={} | cantidad={}",
                evento.getEventId(),
                pedidoId,
                productoId,
                cantidad
        );
    }
}