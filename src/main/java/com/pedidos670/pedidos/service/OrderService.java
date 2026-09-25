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

    public OrderService(OrderRepository orderRepository, CatalogClient catalogClient) {
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
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Transactional
    public Order crearPedido(Order order) {
        order.setEstado(OrderStatus.CREADO);
        order.setFechaCreacion(LocalDateTime.now());

        // Se calcula el total del pedido
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
                .sum();
        order.setTotal(total);

        return orderRepository.save(order);
    }

    @Transactional
    public Order cambiarEstado(Long id, OrderStatus nuevoEstado) {
        Order order = obtenerPorId(id);
        OrderStatus estadoActual = order.getEstado();

        // 1. Validar la transición completa del flujo de estados
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new IllegalStateException("Transición de estado no permitida: de " + estadoActual + " a " + nuevoEstado);
        }

        // 2. Al pasar a ACEPTADO desde CREADO, se reduce el stock en ms-catalogo
        if (nuevoEstado == OrderStatus.ACEPTADO && estadoActual == OrderStatus.CREADO) {
            for (OrderItem item : order.getItems()) {
                catalogClient.reducirStock(item.getProductoId(), item.getCantidad());
            }
        }

        order.setEstado(nuevoEstado);
        return orderRepository.save(order);
    }

    /**
     * Regla de transiciones permitidas:
     * CREADO -> ACEPTADO, CANCELADO
     * ACEPTADO -> EN_PREPARACION, CANCELADO
     * EN_PREPARACION -> DESPACHADO, CANCELADO
     * DESPACHADO -> ENTREGADO, CANCELADO
     * ENTREGADO / CANCELADO -> (Estados finales, no permiten más cambios)
     */
    private boolean esTransicionValida(OrderStatus actual, OrderStatus nuevo) {
        if (actual == nuevo) return true;
        if (nuevo == OrderStatus.CANCELADO) return actual != OrderStatus.ENTREGADO;

        switch (actual) {
            case CREADO:
                return nuevo == OrderStatus.ACEPTADO;
            case ACEPTADO:
                return nuevo == OrderStatus.EN_PREPARACION;
            case EN_PREPARACION:
                return nuevo == OrderStatus.DESPACHADO;
            case DESPACHADO:
                return nuevo == OrderStatus.ENTREGADO;
            default:
                return false;
        }
    }
}