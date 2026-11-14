package com.practicacarlos.devqa_microservicio.controller;

import java.net.URI;
import java.util.List;

import com.practicacarlos.devqa_microservicio.model.Producto;
import com.practicacarlos.devqa_microservicio.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto,
            UriComponentsBuilder uriBuilder) {
        Producto creado = service.crear(producto);
        URI location = uriBuilder.path("/api/productos/{id}")
                .buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable String id,
            @Valid @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
