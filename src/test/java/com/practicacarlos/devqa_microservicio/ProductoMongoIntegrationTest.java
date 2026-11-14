package com.practicacarlos.devqa_microservicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.practicacarlos.devqa_microservicio.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ProductoMongoIntegrationTest {

    @Container
    private static final MongoDBContainer MONGO = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",
                () -> MONGO.getReplicaSetUrl("devqa_productos_test"));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void limpiarProductos() {
        mongoTemplate.dropCollection(Producto.class);
    }

    @Test
    void crudHttpPersistenciaValidacionesYConflictos() throws Exception {
        String payload = """
                {"nombre":"Mouse","codigo":"MOU-01","precio":450.0,"stock":10}
                """;
        String created = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(created).get("id").asText();

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("MOU-01"));
        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mouse"));
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","codigo":"BAD","precio":-1,"stock":-2}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Mouse USB","codigo":"MOU-01","precio":500,"stock":8}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mouse USB"));
        mockMvc.perform(get("/api/productos/no-existe"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/productos/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isNotFound());
    }
}
