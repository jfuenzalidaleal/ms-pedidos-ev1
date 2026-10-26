package com.pedidos670.pedidos.repository;

import com.pedidos670.pedidos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends  JpaRepository<Order, Long> {
    //Permite que el rol CLIENTE consulte unicamente sus pedidos

    List<Order> findByClienteId(String clienteId);

    // Seguimiento público por código (compra sin login)
    Optional<Order> findByCodigoSeguimiento(String codigoSeguimiento);
}