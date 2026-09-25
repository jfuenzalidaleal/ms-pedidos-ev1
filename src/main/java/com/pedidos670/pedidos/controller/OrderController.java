package com.pedidos670.pedidos.controller;

import com.pedidos670.pedidos.model.Order;
import com.pedidos670.pedidos.model.OrderStatus;
import com.pedidos670.pedidos.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Crear nuevo pedido
    @PostMapping
    public ResponseEntity<Order> crearPedido(@RequestBody Order order) {
        Order nuevoPedido = orderService.crearPedido(order);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // Obtener pedidos por cliente
    @GetMapping("/client/{clienteId}")
    public ResponseEntity<List<Order>> obtenerPorCliente(
            @PathVariable String clienteId
    ) {
        return ResponseEntity.ok(
                orderService.obtenerPorCliente(clienteId)
        );
    }

    // Obtener pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                orderService.obtenerPorId(id)
        );
    }

    // Cambiar estado del pedido
    // Este endpoint será usado por OPERADOR / ADMIN desde el BFF
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        try {
            Order pedidoActualizado =
                    orderService.cambiarEstado(id, status);

            return ResponseEntity.ok(pedidoActualizado);

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al actualizar el estado del pedido: "
                                    + e.getMessage()
                    );
        }
    }

    // Cancelar pedido
    // Este endpoint será usado por CLIENTE desde el BFF
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(
            @PathVariable Long id
    ) {
        try {
            Order pedidoCancelado =
                    orderService.cancelarPedido(id);

            return ResponseEntity.ok(pedidoCancelado);

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al cancelar el pedido: "
                                    + e.getMessage()
                    );
        }
    }

    // Obtener todos los pedidos
    @GetMapping
    public ResponseEntity<List<Order>> obtenerTodos() {
        return ResponseEntity.ok(
                orderService.obtenerTodos()
        );
    }
}