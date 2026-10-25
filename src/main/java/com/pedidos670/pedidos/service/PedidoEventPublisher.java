package com.pedidos670.pedidos.service;

import com.pedidos670.pedidos.config.RabbitConfig;
import com.pedidos670.pedidos.event.PedidoAceptadoEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventPublisher(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPedidoAceptado(
            Long pedidoId,
            Long productoId,
            Integer cantidad
    ) {

        PedidoAceptadoEvent evento =
                PedidoAceptadoEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType("PEDIDO_ACEPTADO")
                        .version(1)
                        .pedidoId(pedidoId)
                        .productoId(productoId)
                        .cantidad(cantidad)
                        .occurredAt(LocalDateTime.now())
                        .build();

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_PEDIDOS,
                RabbitConfig.ROUTING_PEDIDO_ACEPTADO,
                evento
        );

        System.out.println(
                "Evento PEDIDO_ACEPTADO enviado a RabbitMQ"
                        + " | pedidoId=" + pedidoId
                        + " | productoId=" + productoId
                        + " | cantidad=" + cantidad
        );
    }
}