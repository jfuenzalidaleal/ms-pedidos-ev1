package com.pedidos670.pedidos.controller;

import  com.pedidos670.pedidos.model.Order;
import  com.pedidos670.pedidos.model.OrderStatus;
import com.pedidos670.pedidos.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import  java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //Crear nuevo pedido (cliente/op/adm)

    @PostMapping
    public ResponseEntity<Order> crearPedido(@ResponseBody Order order){
        Order nuevoPedido = orderService.crearPedido(order);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    //Obtener pedidos especifico (op/adm)
    @GetMapping("/client/{clienteId}")
    public ResponseEntity<List<Order>> obtenerPorCliente(@PathVariable String clienteId){
        return ResponseEntity.ok(orderService.obtenerPorCliente(clienteId));
    }

    //Obtener pedido x ID(op/adsm)
   @GetMapping("/{id}")
   public ResponseEntity<Order> obtenerPorId(@PathVariable Long id) {
       return ResponseEntity.ok(orderService.obtenerPorId(id));
   }

    //Camgbiar estado del pedido (op/adm)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> cambiarEstado(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order pedidoActualizado = orderService.cambiarEstado(id, status);
        return ResponseEntity.ok(pedidoActualizado);
    }


}
