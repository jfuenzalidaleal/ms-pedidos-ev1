package com.pedidos670.pedidos.messaging;

public final class RabbitNames {

    private RabbitNames() {
    }

    public static final String PEDIDOS_EXCHANGE =
            "pedidos.events";

    public static final String CATALOGO_STOCK_QUEUE =
            "catalogo.stock.queue";

    public static final String PEDIDO_ACEPTADO_ROUTING_KEY =
            "pedido.aceptado";
}