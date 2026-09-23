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

    public List<Order> obtenerTodos(){
        return orderRepository.findAll();
    }

    public List<Order> obtenerPorCliente(String clienteId) {
        return orderRepository.findByClienteId(clienteId);
    }

    public Order obtenerPorId(Long id){
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Transactional
    public Order crearPedido(Order order) {
        order.setEstado(OrderStatus.CREADO);
        order.setFechaCreacion(LocalDateTime.now());

        // Se calcula el tyotal del pedido
        double total = order.getItems().stream().mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad()).sum();
        order.setTotal(total);

        return orderRepository.save(order);

    }

    @Transactional
    public Order cambiarEstado(Long id, OrderStatus nuevoEstado) {
        Order order = obtenerPorId(id);
        OrderStatus estadoActual = order.getEstado();

        //Validamos regla de negocio: no se puede despachar sin haber sido aceptado
        if (nuevoEstado == OrderStatus.DESPACHADO && estadoActual != OrderStatus.ACEPTADO && estadoActual != OrderStatus.EN_PREPARACION){
            throw new IllegalStateException("El producto no está Aceptado para ser Despachado");
        }

        //Al pasar a ACEPTADO se reduce el stock
        if (nuevoEstado == OrderStatus.ACEPTADO && estadoActual == OrderStatus.CREADO){
            for (OrderItem item : order.getItems()){
                catalogClient.reducirStock(item.getProductoId(), item.getCantidad());
            }
        }
        order.setEstado(nuevoEstado);
        return orderRepository.save(order);

    }
}
