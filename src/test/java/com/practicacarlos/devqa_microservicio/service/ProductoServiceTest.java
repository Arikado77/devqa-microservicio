package com.practicacarlos.devqa_microservicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.practicacarlos.devqa_microservicio.exception.DatoDuplicadoException;
import com.practicacarlos.devqa_microservicio.exception.RecursoNoEncontradoException;
import com.practicacarlos.devqa_microservicio.model.Producto;
import com.practicacarlos.devqa_microservicio.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto("Teclado", "TEC-01", 899.0, 5);
        producto.setId("p-1");
    }

    @Test
    void listarDevuelveProductosDelRepositorio() {
        when(repository.findAll()).thenReturn(List.of(producto));

        assertThat(service.listar()).containsExactly(producto);
    }

    @Test
    void obtenerPorIdDevuelveProductoExistente() {
        when(repository.findById("p-1")).thenReturn(Optional.of(producto));

        assertThat(service.obtenerPorId("p-1")).isSameAs(producto);
    }

    @Test
    void obtenerPorIdInexistenteLanzaExcepcion() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId("missing"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void crearGuardaProductoConCodigoNuevo() {
        when(repository.findByCodigo("TEC-01")).thenReturn(Optional.empty());
        when(repository.save(producto)).thenReturn(producto);

        assertThat(service.crear(producto)).isSameAs(producto);
        verify(repository).save(producto);
    }

    @Test
    void crearRechazaCodigoExistenteSinGuardar() {
        when(repository.findByCodigo("TEC-01")).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> service.crear(producto))
                .isInstanceOf(DatoDuplicadoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void crearTraduceConflictoUnicoConcurrente() {
        when(repository.findByCodigo("TEC-01")).thenReturn(Optional.empty());
        when(repository.save(producto)).thenThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.crear(producto))
                .isInstanceOf(DatoDuplicadoException.class);
    }

    @Test
    void actualizarConservaCodigoExistenteYActualizaCampos() {
        Producto cambios = new Producto("Teclado mecánico", "TEC-01", 1099.0, 3);
        when(repository.findById("p-1")).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);

        Producto actualizado = service.actualizar("p-1", cambios);

        assertThat(actualizado.getId()).isEqualTo("p-1");
        assertThat(actualizado.getNombre()).isEqualTo("Teclado mecánico");
        assertThat(actualizado.getCodigo()).isEqualTo("TEC-01");
        assertThat(actualizado.getPrecio()).isEqualTo(1099.0);
        assertThat(actualizado.getStock()).isEqualTo(3);
        verify(repository, never()).findByCodigo("TEC-01");
    }

    @Test
    void actualizarRechazaCodigoDeOtroProducto() {
        Producto cambios = new Producto("Monitor", "MON-01", 3000.0, 2);
        when(repository.findById("p-1")).thenReturn(Optional.of(producto));
        when(repository.findByCodigo("MON-01")).thenReturn(Optional.of(
                new Producto("Otro", "MON-01", 1.0, 0)));

        assertThatThrownBy(() -> service.actualizar("p-1", cambios))
                .isInstanceOf(DatoDuplicadoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarTraduceConflictoUnicoConcurrente() {
        Producto cambios = new Producto("Teclado", "TEC-02", 899.0, 5);
        when(repository.findById("p-1")).thenReturn(Optional.of(producto));
        when(repository.findByCodigo("TEC-02")).thenReturn(Optional.empty());
        when(repository.save(producto)).thenThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.actualizar("p-1", cambios))
                .isInstanceOf(DatoDuplicadoException.class);
    }

    @Test
    void actualizarInexistenteLanzaExcepcion() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("missing", producto))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void eliminarBorraProductoExistente() {
        when(repository.findById("p-1")).thenReturn(Optional.of(producto));

        service.eliminar("p-1");

        verify(repository).delete(producto);
    }

    @Test
    void eliminarInexistenteLanzaExcepcionSinBorrar() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("missing"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repository, never()).delete(any(Producto.class));
    }
}
