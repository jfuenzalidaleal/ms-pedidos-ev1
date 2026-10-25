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

    private final PedidoEventPublisher pedidoEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            CatalogClient catalogClient,
            PedidoEventPublisher pedidoEventPublisher
    ) {

        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
        this.pedidoEventPublisher = pedidoEventPublisher;
    }

    public List<Order> obtenerTodos() {

        return orderRepository.findAll();
    }

    public List<Order> obtenerPorCliente(
            String clienteId
    ) {

        return orderRepository
                .findByClienteId(clienteId);
    }

    public Order obtenerPorId(
            Long id
    ) {

        return orderRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Pedido no encontrado con ID: "
                                        + id
                        )
                );
    }

    @Transactional
    public Order crearPedido(
            Order order
    ) {

        order.setEstado(
                OrderStatus.CREADO
        );

        order.setFechaCreacion(
                LocalDateTime.now()
        );

        double total =
                order.getItems()
                        .stream()
                        .mapToDouble(
                                item ->
                                        item.getPrecioUnitario()
                                                * item.getCantidad()
                        )
                        .sum();

        order.setTotal(total);

        return orderRepository.save(order);
    }

    @Transactional
    public Order cambiarEstado(
            Long id,
            OrderStatus nuevoEstado
    ) {

        Order order =
                obtenerPorId(id);

        OrderStatus estadoActual =
                order.getEstado();

        /*
         * CANCELADO no se procesa desde este método.
         */
        if (
                nuevoEstado
                        == OrderStatus.CANCELADO
        ) {

            throw new IllegalStateException(
                    "La cancelación debe realizarse mediante "
                            + "el endpoint de cancelar pedido"
            );
        }

        /*
         * Validamos que la transición sea correcta.
         */
        if (
                !esTransicionValida(
                        estadoActual,
                        nuevoEstado
                )
        ) {

            throw new IllegalStateException(
                    "Transición de estado no permitida: de "
                            + estadoActual
                            + " a "
                            + nuevoEstado
            );
        }

        /*
         * ==================================================
         * FLUJO TEMPORAL
         * ==================================================
         *
         * Todavía mantenemos el descuento directo de stock
         * mediante CatalogClient.
         *
         * Esto evita romper el sistema mientras probamos
         * RabbitMQ.
         *
         * Después eliminaremos esta llamada y ms-catalogo
         * consumirá el evento desde RabbitMQ.
         */
        if (
                estadoActual
                        == OrderStatus.CREADO
                        &&
                        nuevoEstado
                                == OrderStatus.ACEPTADO
        ) {

            for (
                    OrderItem item :
                    order.getItems()
            ) {

                catalogClient.reducirStock(
                        item.getProductoId(),
                        item.getCantidad()
                );
            }
        }

        /*
         * Guardamos primero el nuevo estado.
         */
        order.setEstado(nuevoEstado);

        Order guardado =
                orderRepository.save(order);

        /*
         * ==================================================
         * RABBITMQ
         * ==================================================
         *
         * Si el pedido acaba de pasar de:
         *
         * CREADO -> ACEPTADO
         *
         * publicamos un evento por cada producto.
         *
         * POR AHORA ms-catalogo todavía NO lo consume.
         */
        if (
                estadoActual
                        == OrderStatus.CREADO
                        &&
                        nuevoEstado
                                == OrderStatus.ACEPTADO
        ) {

            for (
                    OrderItem item :
                    guardado.getItems()
            ) {

                pedidoEventPublisher
                        .publicarPedidoAceptado(
                                guardado.getId(),
                                item.getProductoId(),
                                item.getCantidad()
                        );
            }
        }

        return guardado;
    }

    /*
     * Cancelación realizada por CLIENTE.
     */
    @Transactional
    public Order cancelarPedido(
            Long id
    ) {

        Order order =
                obtenerPorId(id);

        if (
                order.getEstado()
                        != OrderStatus.CREADO
        ) {

            throw new IllegalStateException(
                    "Solo se puede cancelar un pedido "
                            + "que esté en estado CREADO"
            );
        }

        order.setEstado(
                OrderStatus.CANCELADO
        );

        return orderRepository.save(order);
    }

    /*
     * Cancelación realizada por ADMIN.
     */
    @Transactional
    public Order cancelarPedidoAdmin(
            Long id
    ) {

        Order order =
                obtenerPorId(id);

        if (
                order.getEstado()
                        == OrderStatus.CANCELADO
        ) {

            throw new IllegalStateException(
                    "El pedido ya se encuentra cancelado"
            );
        }

        if (
                order.getEstado()
                        == OrderStatus.ENTREGADO
        ) {

            throw new IllegalStateException(
                    "No se puede cancelar un pedido "
                            + "que ya fue entregado"
            );
        }

        order.setEstado(
                OrderStatus.CANCELADO
        );

        return orderRepository.save(order);
    }

    /*
     * Flujo permitido:
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

                return nuevo
                        == OrderStatus.ACEPTADO;

            case ACEPTADO:

                return nuevo
                        == OrderStatus.EN_PREPARACION;

            case EN_PREPARACION:

                return nuevo
                        == OrderStatus.DESPACHADO;

            case DESPACHADO:

                return nuevo
                        == OrderStatus.ENTREGADO;

            case ENTREGADO:
            case CANCELADO:
            default:

                return false;
        }
    }
}