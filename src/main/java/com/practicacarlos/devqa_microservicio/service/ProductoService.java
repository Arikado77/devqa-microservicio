package com.practicacarlos.devqa_microservicio.service;

import java.util.List;

import com.practicacarlos.devqa_microservicio.exception.DatoDuplicadoException;
import com.practicacarlos.devqa_microservicio.exception.RecursoNoEncontradoException;
import com.practicacarlos.devqa_microservicio.model.Producto;
import com.practicacarlos.devqa_microservicio.repository.ProductoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el producto con id: " + id));
    }

    public Producto crear(Producto producto) {
        if (repository.findByCodigo(producto.getCodigo()).isPresent()) {
            throw new DatoDuplicadoException(
                    "Ya existe un producto con codigo: " + producto.getCodigo());
        }
        try {
            return repository.save(producto);
        } catch (DuplicateKeyException exception) {
            throw new DatoDuplicadoException(
                    "Ya existe un producto con codigo: " + producto.getCodigo());
        }
    }

    public Producto actualizar(String id, Producto cambios) {
        Producto actual = obtenerPorId(id);
        if (!actual.getCodigo().equals(cambios.getCodigo())
                && repository.findByCodigo(cambios.getCodigo()).isPresent()) {
            throw new DatoDuplicadoException(
                    "Ya existe un producto con codigo: " + cambios.getCodigo());
        }
        actual.setNombre(cambios.getNombre());
        actual.setCodigo(cambios.getCodigo());
        actual.setPrecio(cambios.getPrecio());
        actual.setStock(cambios.getStock());
        try {
            return repository.save(actual);
        } catch (DuplicateKeyException exception) {
            throw new DatoDuplicadoException(
                    "Ya existe un producto con codigo: " + cambios.getCodigo());
        }
    }

    public void eliminar(String id) {
        Producto producto = obtenerPorId(id);
        repository.delete(producto);
    }
}
