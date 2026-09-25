package com.pedidos670.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "ms-catalogo",
        url = "${catalog.service.url}/api/catalogo/productos"
)
public interface CatalogClient {

    @PutMapping("/{id}/stock")
    void reducirStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);
}