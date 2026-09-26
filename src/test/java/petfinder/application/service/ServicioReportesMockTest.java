package petfinder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import petfinder.application.port.salida.RepositorioReportes;
import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Pruebas de {@link ServicioReportes} con el repositorio simulado.
 *
 * El repositorio en memoria del Corte 1 no deja ver si el servicio intentó
 * guardar algo que no debía; un doble de Mockito sí. Por eso cada caso de
 * error verifica que {@code guardar} nunca se llamó: un reporte inválido que
 * alcanzara la base sería un caso fantasma en el mapa.
 *
 * Los creadores del Factory Method son los reales: lo que se aísla es la
 * persistencia, no las reglas de negocio. El servicio se construye en cada
 * prueba porque su mapa de creadores es justamente lo que algunas varían.
 */
@ExtendWith(MockitoExtension.class)
class ServicioReportesMockTest {

    @Mock
    private RepositorioReportes repositorio;

    private ServicioReportes servicioConCreadorDePerdidas() {
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(new GeneradorIdReportes()));
        return new ServicioReportes(repositorio, creadores);
    }

    private static SolicitudReporte perdidaEn(String zona) {
        return SolicitudReporte.paraPerdida(new Ubicacion(zona, "Parque de la 147"), "Se escapó en la tarde",
                new Contacto("Camila", "3001234567"), new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"));
    }

    @Test
    @DisplayName("Registrar un tipo sin creador lanza OperacionNoPermitidaException y no guarda nada")
    void registrarConTipoSinCreadorLanzaOperacionNoPermitida() {
        // Arrange
        ServicioReportes servicio = new ServicioReportes(repositorio, Map.of());

        // Act
        assertThrows(OperacionNoPermitidaException.class,
                () -> servicio.registrar(TipoReporte.PERDIDA, perdidaEn("Cedritos")));

        // Assert
        verify(repositorio, never()).guardar(any());
    }

    @Test
    @DisplayName("Una pérdida sin zona lanza DatosInvalidosException y no guarda nada")
    void registrarConDatosInvalidosNoGuardaNada() {
        // Arrange
        ServicioReportes servicio = servicioConCreadorDePerdidas();

        // Act
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> servicio.registrar(TipoReporte.PERDIDA, perdidaEn(null)));

        // Assert
        assertEquals("Se requiere la zona o barrio donde ocurrió el hecho", error.getMessage());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    @DisplayName("Una pérdida válida se guarda una vez y se devuelve el mismo reporte guardado")
    void registrarValidoGuardaElReporteCreado() {
        // Arrange
        ServicioReportes servicio = servicioConCreadorDePerdidas();

        // Act
        ReporteMascota creado = servicio.registrar(TipoReporte.PERDIDA, perdidaEn("Cedritos"));

        // Assert
        verify(repositorio).guardar(same(creado));
        assertEquals(EstadoReporte.ACTIVO, creado.getEstado());
    }

    @Test
    @DisplayName("Resolver un caso guarda ese mismo reporte ya en estado RESUELTO")
    void resolverGuardaElReporteActualizado() {
        // Arrange
        ServicioReportes servicio = servicioConCreadorDePerdidas();
        ReportePerdida activo = ReportePerdida.reconstruir("PF-010", new Ubicacion("Cedritos", null),
                "Se escapó en la tarde", new Mascota("Luna", "perro", null, null, null),
                new Contacto("Camila", "3001234567"), LocalDateTime.of(2026, 9, 24, 16, 30),
                EstadoReporte.ACTIVO, List.of());
        when(repositorio.buscarPorId("PF-010")).thenReturn(Optional.of(activo));

        // Act
        servicio.resolver("PF-010");

        // Assert
        verify(repositorio).guardar(same(activo));
        assertEquals(EstadoReporte.RESUELTO, activo.getEstado());
    }

    @Test
    @DisplayName("Resolver un caso inexistente lanza ReporteNoEncontradoException y no guarda nada")
    void resolverInexistenteNoGuardaNada() {
        // Arrange
        ServicioReportes servicio = servicioConCreadorDePerdidas();
        when(repositorio.buscarPorId("PF-999999")).thenReturn(Optional.empty());

        // Act
        assertThrows(ReporteNoEncontradoException.class, () -> servicio.resolver("PF-999999"));

        // Assert
        verify(repositorio, never()).guardar(any());
    }
}
