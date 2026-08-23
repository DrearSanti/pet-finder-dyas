package petfinder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;
import petfinder.domain.repository.RepositorioReportesEnMemoria;

/**
 * Pruebas del ciclo de vida de un reporte: creación polimórfica, consulta de
 * activos y protección de las transiciones de estado.
 *
 * Cada prueba arma su propio servicio desde cero en el @BeforeEach: los
 * reportes se guardan en memoria y compartir instancias entre pruebas haría
 * que el orden de ejecución cambiara los resultados.
 */
class ServicioReportesTest {

    private ServicioReportes servicio;

    @BeforeEach
    void prepararServicio() {
        GeneradorIdReportes generadorId = new GeneradorIdReportes();
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generadorId));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generadorId));
        servicio = new ServicioReportes(new RepositorioReportesEnMemoria(), creadores);
    }

    @Test
    @DisplayName("Registrar una perdida produce un ReportePerdida en estado ACTIVO")
    void registrarPerdidaCreaElTipoCorrecto() {
        ReporteMascota reporte = servicio.registrar(TipoReporte.PERDIDA, solicitudPerdida());

        // La aserción sobre el tipo concreto es la evidencia de que el Factory
        // Method eligió el producto correcto sin que el servicio lo nombrara.
        assertInstanceOf(ReportePerdida.class, reporte);
        assertEquals(EstadoReporte.ACTIVO, reporte.getEstado());
    }

    @Test
    @DisplayName("Registrar una encontrada produce un ReporteEncontrada en estado ACTIVO")
    void registrarEncontradaCreaElTipoCorrecto() {
        ReporteMascota reporte = servicio.registrar(TipoReporte.ENCONTRADA, solicitudEncontrada());

        assertInstanceOf(ReporteEncontrada.class, reporte);
        assertEquals(EstadoReporte.ACTIVO, reporte.getEstado());
    }

    @Test
    @DisplayName("Listar activos omite los reportes resueltos y cerrados")
    void listarActivosOmiteLosTerminados() {
        ReporteMascota aResolver = servicio.registrar(TipoReporte.PERDIDA, solicitudPerdida());
        ReporteMascota aCerrar = servicio.registrar(TipoReporte.ENCONTRADA, solicitudEncontrada());
        ReporteMascota vigente = servicio.registrar(TipoReporte.PERDIDA, solicitudPerdida());

        servicio.resolver(aResolver.getId());
        servicio.cerrar(aCerrar.getId());

        List<ReporteMascota> activos = servicio.listarActivos();

        // Se comprueban las dos caras del filtro: que quede el vigente y que
        // no queden los terminados. Contar solo el tamaño dejaría pasar un
        // filtro que devolviera el reporte equivocado.
        assertEquals(1, activos.size());
        assertTrue(activos.contains(vigente));
    }

    @Test
    @DisplayName("Un reporte que no existe produce ReporteNoEncontradoException")
    void consultarReporteInexistenteFalla() {
        assertThrows(ReporteNoEncontradoException.class, () -> servicio.consultar("PF-999"));
        assertThrows(ReporteNoEncontradoException.class, () -> servicio.resolver("PF-999"));
    }

    @Test
    @DisplayName("Un reporte terminado no admite otra transicion de estado")
    void transicionInvalidaEsRechazada() {
        ReporteMascota reporte = servicio.registrar(TipoReporte.PERDIDA, solicitudPerdida());
        servicio.resolver(reporte.getId());

        // RESUELTO y CERRADO son terminales: ni se repite la transicion ni se
        // salta de uno al otro.
        assertThrows(OperacionNoPermitidaException.class,
                () -> servicio.resolver(reporte.getId()));
        assertThrows(OperacionNoPermitidaException.class,
                () -> servicio.cerrar(reporte.getId()));
    }

    private SolicitudReporte solicitudPerdida() {
        return SolicitudReporte.paraPerdida(
                new Ubicacion("Cedritos", "Parque de la 140"),
                "Se solto de la correa",
                new Contacto("Camila Rojas", "3001234567"),
                new Mascota("Luna", "Perro", "Criolla", "Blanca", "Mancha negra"));
    }

    private SolicitudReporte solicitudEncontrada() {
        return SolicitudReporte.paraEncontrada(
                new Ubicacion("Bella Suiza", "Frente a la panaderia"),
                "Estaba solo en el anden",
                new Contacto("Andres Melo", "andres@correo.com"),
                "Gato gris con collar azul");
    }
}