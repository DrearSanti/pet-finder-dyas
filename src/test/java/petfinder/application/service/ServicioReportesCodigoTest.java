package petfinder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.adaptadores.salida.persistencia.memoria.RepositorioReportesEnMemoria;
import petfinder.application.port.entrada.ReportePublicado;
import petfinder.domain.exception.CodigoGestionInvalidoException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.CodigoGestion;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Código de gestión (S9, amenaza T1): solo quien publicó, o el administrador,
 * resuelve o cierra un caso. Cada prueba arma su propio servicio y su propio
 * repositorio en memoria.
 */
class ServicioReportesCodigoTest {

    private static final String CODIGO_ADMIN = "codigo-de-administrador-de-prueba-123";

    private static ServicioReportes servicio(String huellaAdmin) {
        GeneradorIdReportes generadorId = new GeneradorIdReportes();
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generadorId));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generadorId));
        return new ServicioReportes(new RepositorioReportesEnMemoria(), creadores, huellaAdmin);
    }

    private static SolicitudReporte perdida() {
        return SolicitudReporte.paraPerdida(new Ubicacion("Chía", "Parque principal"), "Se escapó",
                new Contacto("Ana", "3001112233"), new Mascota("Max", "perro", "criollo", "café", "collar rojo"));
    }

    @Test
    @DisplayName("Publicar devuelve un código, y el caso guarda su huella y no el código")
    void publicarDevuelveElCodigoYGuardaLaHuella() {
        // Arrange
        ServicioReportes servicio = servicio(null);
        // Act
        ReportePublicado publicado = servicio.publicar(TipoReporte.PERDIDA, perdida());
        // Assert
        ReporteMascota guardado = servicio.consultar(publicado.reporte().getId());
        assertEquals(CodigoGestion.huellaDe(publicado.codigoGestion()), guardado.getHuellaCodigo());
        assertNotEquals(publicado.codigoGestion(), guardado.getHuellaCodigo());
    }

    @Test
    @DisplayName("Cerrar sin código se rechaza y el caso sigue activo")
    void sinCodigo() {
        // Arrange
        ServicioReportes servicio = servicio(null);
        String id = servicio.publicar(TipoReporte.PERDIDA, perdida()).reporte().getId();
        // Act y Assert
        assertThrows(CodigoGestionInvalidoException.class, () -> servicio.cerrar(id, null));
        assertThrows(CodigoGestionInvalidoException.class, () -> servicio.cerrar(id, ""));
        assertEquals(EstadoReporte.ACTIVO, servicio.consultar(id).getEstado());
    }

    @Test
    @DisplayName("El código de otro caso no sirve para cerrar este")
    void codigoDeOtroCaso() {
        // Arrange
        ServicioReportes servicio = servicio(null);
        String id = servicio.publicar(TipoReporte.PERDIDA, perdida()).reporte().getId();
        String codigoAjeno = servicio.publicar(TipoReporte.PERDIDA, perdida()).codigoGestion();
        // Act y Assert
        assertThrows(CodigoGestionInvalidoException.class, () -> servicio.resolver(id, codigoAjeno));
        assertEquals(EstadoReporte.ACTIVO, servicio.consultar(id).getEstado());
    }

    @Test
    @DisplayName("Con su propio código el caso se resuelve")
    void codigoCorrecto() {
        // Arrange
        ServicioReportes servicio = servicio(null);
        ReportePublicado publicado = servicio.publicar(TipoReporte.PERDIDA, perdida());
        String id = publicado.reporte().getId();
        // Act
        servicio.resolver(id, publicado.codigoGestion());
        // Assert
        assertEquals(EstadoReporte.RESUELTO, servicio.consultar(id).getEstado());
    }

    @Test
    @DisplayName("El administrador cierra cualquier caso, también uno publicado sin código")
    void administrador() {
        // Arrange
        ServicioReportes servicio = servicio(CodigoGestion.huellaDe(CODIGO_ADMIN));
        String conCodigo = servicio.publicar(TipoReporte.PERDIDA, perdida()).reporte().getId();
        @SuppressWarnings("deprecation")
        String sinCodigo = servicio.registrar(TipoReporte.PERDIDA, perdida()).getId();
        // Act
        servicio.cerrar(conCodigo, CODIGO_ADMIN);
        servicio.cerrar(sinCodigo, CODIGO_ADMIN);
        // Assert
        assertEquals(EstadoReporte.CERRADO, servicio.consultar(conCodigo).getEstado());
        assertEquals(EstadoReporte.CERRADO, servicio.consultar(sinCodigo).getEstado());
    }

    @Test
    @DisplayName("Sin administrador configurado, un código vacío nunca cierra un caso sin código")
    void administradorVacio() {
        // Arrange
        ServicioReportes servicio = servicio("");
        @SuppressWarnings("deprecation")
        String sinCodigo = servicio.registrar(TipoReporte.PERDIDA, perdida()).getId();
        // Act y Assert
        assertThrows(CodigoGestionInvalidoException.class, () -> servicio.cerrar(sinCodigo, ""));
        assertThrows(CodigoGestionInvalidoException.class, () -> servicio.cerrar(sinCodigo, null));
        assertEquals(EstadoReporte.ACTIVO, servicio.consultar(sinCodigo).getEstado());
    }

    @Test
    @DisplayName("Un caso que no existe responde no encontrado antes de mirar el código")
    void noExisteAntesQueCodigo() {
        // Arrange
        ServicioReportes servicio = servicio(null);
        // Act y Assert
        assertThrows(ReporteNoEncontradoException.class, () -> servicio.cerrar("PF-999999", "cualquiera"));
    }
}
