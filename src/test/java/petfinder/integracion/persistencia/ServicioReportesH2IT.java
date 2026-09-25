package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.salida.RepositorioReportes;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Frontera caso de uso + adaptador + base de datos, sin ningún mock. Se
 * inyecta el puerto, no la clase del servicio.
 */
@SpringBootTest(properties = {"petfinder.datos-ejemplo=false", "ANTHROPIC_API_KEY="})
@Transactional
class ServicioReportesH2IT {

    @Autowired
    private GestionReportes gestion;

    @Autowired
    private RepositorioReportes repositorio;

    private SolicitudReporte solicitud() {
        return SolicitudReporte.paraPerdida(new Ubicacion("Chía", "Parque principal"), "Se escapó",
                new Contacto("Ana", "3001112233"), new Mascota("Max", "perro", "criollo", "café", "collar rojo"));
    }

    @Test
    @DisplayName("La aplicación usa el adaptador H2 como repositorio")
    void usaElAdaptadorH2() {
        assertTrue(repositorio instanceof RepositorioReportesH2);
    }

    @Test
    @DisplayName("Registrar y consultar un reporte pasa por la base de datos")
    void registrarYConsultar() {
        // Act
        ReporteMascota creado = gestion.registrar(TipoReporte.PERDIDA, solicitud());
        // Assert
        assertEquals(creado.getId(), gestion.consultar(creado.getId()).getId());
        assertTrue(gestion.listarActivos().stream().anyMatch(r -> r.getId().equals(creado.getId())));
    }

    @Test
    @DisplayName("Resolver un reporte queda persistido")
    void resolverQuedaPersistido() {
        // Arrange
        ReporteMascota creado = gestion.registrar(TipoReporte.PERDIDA, solicitud());
        // Act
        gestion.resolver(creado.getId());
        // Assert
        assertEquals(EstadoReporte.RESUELTO, gestion.consultar(creado.getId()).getEstado());
    }
}
