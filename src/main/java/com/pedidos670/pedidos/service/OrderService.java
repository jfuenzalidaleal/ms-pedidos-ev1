package com.pedidos670.pedidos.service;

import com.pedidos670.pedidos.client.CatalogClient;
import com.pedidos670.pedidos.model.Order;
import com.pedidos670.pedidos.model.OrderItem;
import com.pedidos670.pedidos.model.OrderStatus;
import com.pedidos670.pedidos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderService(
            OrderRepository orderRepository,
            CatalogClient catalogClient
    ) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    public List<Order> obtenerTodos() {
        return orderRepository.findAll();
    }

    public List<Order> obtenerPorCliente(String clienteId) {
        return orderRepository.findByClienteId(clienteId);
    }

    public Order obtenerPorId(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Pedido no encontrado con ID: " + id
                        )
                );
    }

    @Transactional
    public Order crearPedido(Order order) {

        order.setEstado(OrderStatus.CREADO);
        order.setFechaCreacion(LocalDateTime.now());

        double total = order.getItems()
                .stream()
                .mapToDouble(item ->
                        item.getPrecioUnitario()
                                * item.getCantidad()
                )
                .sum();

        order.setTotal(total);

        return orderRepository.save(order);
    }

    /**
     * Solo OPERADOR / ADMIN deberían llegar a este método
     * a través del BFF.
     */
    @Transactional
    public Order cambiarEstado(
            Long id,
            OrderStatus nuevoEstado
    ) {

        Order order = obtenerPorId(id);

        OrderStatus estadoActual =
                order.getEstado();

        // CANCELADO tiene un endpoint separado para CLIENTE.
        if (nuevoEstado == OrderStatus.CANCELADO) {
            throw new IllegalStateException(
                    "La cancelación debe realizarse mediante el endpoint de cancelar pedido"
            );
        }

        if (!esTransicionValida(
                estadoActual,
                nuevoEstado
        )) {
            throw new IllegalStateException(
                    "Transición de estado no permitida: de "
                            + estadoActual
                            + " a "
                            + nuevoEstado
            );
        }

        // El stock se descuenta una sola vez:
        // cuando el pedido pasa de CREADO a ACEPTADO.
        if (
                estadoActual == OrderStatus.CREADO
                        && nuevoEstado == OrderStatus.ACEPTADO
        ) {

            for (OrderItem item : order.getItems()) {

                catalogClient.reducirStock(
                        item.getProductoId(),
                        item.getCantidad()
                );
            }
        }

        order.setEstado(nuevoEstado);

        return orderRepository.save(order);
    }

    /**
     * Cancelación exclusiva para el flujo del CLIENTE.
     *
     * El cliente solo puede cancelar mientras el pedido
     * todavía está en estado CREADO.
     */
    @Transactional
    public Order cancelarPedido(Long id) {

        Order order = obtenerPorId(id);

        if (order.getEstado() != OrderStatus.CREADO) {

            throw new IllegalStateException(
                    "Solo se puede cancelar un pedido que esté en estado CREADO"
            );
        }

        order.setEstado(
                OrderStatus.CANCELADO
        );

        return orderRepository.save(order);
    }
    @Transactional
    public Order cancelarPedidoAdmin(Long id) {

        Order order = obtenerPorId(id);

        if (order.getEstado() == OrderStatus.CANCELADO) {
            throw new IllegalStateException(
                    "El pedido ya se encuentra cancelado"
            );
        }

        if (order.getEstado() == OrderStatus.ENTREGADO) {
            throw new IllegalStateException(
                    "No se puede cancelar un pedido que ya fue entregado"
            );
        }

        order.setEstado(OrderStatus.CANCELADO);

        return orderRepository.save(order);
    }

    /**
     * Flujo válido de OPERADOR / ADMIN:
     *
     * CREADO
     *   -> ACEPTADO
     *
     * ACEPTADO
     *   -> EN_PREPARACION
     *
     * EN_PREPARACION
     *   -> DESPACHADO
     *
     * DESPACHADO
     *   -> ENTREGADO
     *
     * ENTREGADO y CANCELADO son estados finales.
     */
    private boolean esTransicionValida(
            OrderStatus actual,
            OrderStatus nuevo
    ) {

        if (actual == nuevo) {
            return false;
        }

        switch (actual) {

            case CREADO:
                return nuevo == OrderStatus.ACEPTADO;

            case ACEPTADO:
                return nuevo == OrderStatus.EN_PREPARACION;

            case EN_PREPARACION:
                return nuevo == OrderStatus.DESPACHADO;

            case DESPACHADO:
                return nuevo == OrderStatus.ENTREGADO;

            case ENTREGADO:
            case CANCELADO:
            default:
                return false;
        }
    }
}