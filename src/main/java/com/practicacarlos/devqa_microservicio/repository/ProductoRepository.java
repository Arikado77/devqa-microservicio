package com.practicacarlos.devqa_microservicio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.practicacarlos.devqa_microservicio.model.Producto;

import java.util.Optional;

public interface ProductoRepository
        extends MongoRepository<Producto, String> {

    Optional<Producto> findByCodigo(String codigo);

}
