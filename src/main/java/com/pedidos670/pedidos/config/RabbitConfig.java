package com.pedidos670.pedidos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_PEDIDOS =
            "pedidos.events";

    public static final String QUEUE_CATALOGO_STOCK =
            "catalogo.stock.queue";

    public static final String ROUTING_PEDIDO_ACEPTADO =
            "pedido.aceptado";

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(
                EXCHANGE_PEDIDOS,
                true,
                false
        );
    }

    @Bean
    public Queue catalogoStockQueue() {
        return QueueBuilder
                .durable(QUEUE_CATALOGO_STOCK)
                .build();
    }

    @Bean
    public Binding catalogoStockBinding(
            Queue catalogoStockQueue,
            TopicExchange pedidosExchange
    ) {
        return BindingBuilder
                .bind(catalogoStockQueue)
                .to(pedidosExchange)
                .with(ROUTING_PEDIDO_ACEPTADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /*
     * Fuerza la inicialización de RabbitMQ al arrancar.
     *
     * Esto permite detectar problemas de conexión
     * antes de procesar un pedido real.
     */
    @Bean
    public ApplicationRunner initializeRabbitMQ(
            ConnectionFactory connectionFactory,
            TopicExchange pedidosExchange,
            Queue catalogoStockQueue,
            Binding catalogoStockBinding
    ) {

        return args -> {

            RabbitAdmin rabbitAdmin =
                    new RabbitAdmin(connectionFactory);

            rabbitAdmin.declareExchange(
                    pedidosExchange
            );

            rabbitAdmin.declareQueue(
                    catalogoStockQueue
            );

            rabbitAdmin.declareBinding(
                    catalogoStockBinding
            );

            System.out.println(
                    "RabbitMQ inicializado correctamenteeee"
            );

            System.out.println(
                    "Exchange: "
                            + EXCHANGE_PEDIDOS
            );

            System.out.println(
                    "Queue: "
                            + QUEUE_CATALOGO_STOCK
            );
        };
    }
}