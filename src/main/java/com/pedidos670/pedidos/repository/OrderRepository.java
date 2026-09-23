package com.pedidos670.pedidos.repository;

import com.pedidos670.pedidos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends  JpaRepository<Order, Long> {
    //Permite que el rol CLIENTE consulte unicamente sus propios pedidos

    List<Order> findByClienteId(String clienteId);
}
