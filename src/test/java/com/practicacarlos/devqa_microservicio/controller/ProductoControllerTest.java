package com.practicacarlos.devqa_microservicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.practicacarlos.devqa_microservicio.exception.DatoDuplicadoException;
import com.practicacarlos.devqa_microservicio.exception.RecursoNoEncontradoException;
import com.practicacarlos.devqa_microservicio.model.Producto;
import com.practicacarlos.devqa_microservicio.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService service;

    @Test
    void listarResponde200() throws Exception {
        when(service.listar()).thenReturn(List.of(producto("p-1")));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("TEC-01"));
    }

    @Test
    void obtenerResponde200() throws Exception {
        when(service.obtenerPorId("p-1")).thenReturn(producto("p-1"));

        mockMvc.perform(get("/api/productos/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void obtenerInexistenteResponde404() throws Exception {
        when(service.obtenerPorId("missing"))
                .thenThrow(new RecursoNoEncontradoException("No se encontró el producto"));

        mockMvc.perform(get("/api/productos/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/productos/missing"));
    }

    @Test
    void crearResponde201YLocation() throws Exception {
        when(service.crear(any(Producto.class))).thenReturn(producto("p-1"));

        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProducto("TEC-01")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/productos/p-1"))
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void crearConDatosInvalidosResponde400ConDetalle() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","codigo":"","precio":0,"stock":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.nombre").exists())
                .andExpect(jsonPath("$.details.codigo").exists())
                .andExpect(jsonPath("$.details.precio").exists())
                .andExpect(jsonPath("$.details.stock").exists());
    }

    @Test
    void crearConJsonMalformedResponde400() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo JSON no es válido"));
    }

    @Test
    void crearDuplicadoResponde409() throws Exception {
        when(service.crear(any(Producto.class)))
                .thenThrow(new DatoDuplicadoException("Código duplicado"));

        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProducto("TEC-01")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void actualizarResponde200() throws Exception {
        when(service.actualizar(eq("p-1"), any(Producto.class))).thenReturn(producto("p-1"));

        mockMvc.perform(put("/api/productos/p-1").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProducto("TEC-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void eliminarResponde204() throws Exception {
        doNothing().when(service).eliminar("p-1");

        mockMvc.perform(delete("/api/productos/p-1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar("p-1");
    }

    @Test
    void errorDeClaveUnicaResponde409() throws Exception {
        when(service.crear(any(Producto.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("driver detail"));

        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProducto("TEC-01")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El codigo ya está registrado"));
    }

    @Test
    void borrarInexistenteResponde404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No se encontró el producto"))
                .when(service).eliminar("missing");

        mockMvc.perform(delete("/api/productos/missing"))
                .andExpect(status().isNotFound());
    }

    private Producto producto(String id) {
        Producto producto = new Producto("Teclado", "TEC-01", 899.0, 5);
        producto.setId(id);
        return producto;
    }

    private String jsonProducto(String codigo) {
        return """
                {"nombre":"Teclado","codigo":"%s","precio":899.0,"stock":5}
                """.formatted(codigo);
    }
}
