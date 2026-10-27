package com.pedidos670.pedidos.config;

import com.pedidos670.pedidos.messaging.RabbitNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(
                RabbitNames.PEDIDOS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue catalogoStockQueue() {
        return QueueBuilder
                .durable(RabbitNames.CATALOGO_STOCK_QUEUE)
                .build();
    }

    @Bean
    public Binding catalogoStockBinding(
            @Qualifier("catalogoStockQueue") Queue catalogoStockQueue,
            @Qualifier("pedidosExchange") TopicExchange pedidosExchange
    ) {
        return BindingBuilder
                .bind(catalogoStockQueue)
                .to(pedidosExchange)
                .with(RabbitNames.PEDIDO_ACEPTADO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}