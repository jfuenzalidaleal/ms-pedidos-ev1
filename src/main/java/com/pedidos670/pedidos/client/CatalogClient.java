package com.pedidos670.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "ms.catalogo.ev1", url = "${catalog.service.url}")
public interface CatalogClient {
    @PutMapping("/api/catalogo/productos/{id}/reduce-stock")
    void reducirStock(@PathVariable("id") Long productoId, @RequestParam("cantidad") Integer cantidad);
}
