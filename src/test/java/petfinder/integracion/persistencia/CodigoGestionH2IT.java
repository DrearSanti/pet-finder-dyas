package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.ReportePublicado;
import petfinder.domain.exception.CodigoGestionInvalidoException;
import petfinder.domain.model.CodigoGestion;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * El código de gestión contra la base real (S9): en la tabla queda la huella
 * y nunca el código, y la huella sobrevive a guardar y leer de nuevo. Se mira
 * la fila con SQL directo, porque es lo que vería quien leyera la base.
 */
@SpringBootTest(properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class CodigoGestionH2IT {

    @Autowired
    private GestionReportes gestion;

    @Autowired
    private JdbcTemplate jdbc;

    private SolicitudReporte solicitud() {
        return SolicitudReporte.paraPerdida(new Ubicacion("Chía", "Parque principal"), "Se escapó",
                new Contacto("Ana", "3001112233"), new Mascota("Max", "perro", "criollo", "café", "collar rojo"));
    }

    @Test
    @DisplayName("En la base se guarda la huella del código y en ninguna columna el código")
    void seGuardaLaHuellaYNoElCodigo() {
        // Arrange
        ReportePublicado publicado = gestion.publicar(TipoReporte.PERDIDA, solicitud());
        String id = publicado.reporte().getId();
        // Act
        Map<String, Object> fila = jdbc.queryForMap("select * from reportes where id = ?", id);
        // Assert
        assertEquals(CodigoGestion.huellaDe(publicado.codigoGestion()), fila.get("HUELLA_CODIGO"));
        assertFalse(fila.values().stream().anyMatch(v -> v != null && v.toString().contains(publicado.codigoGestion())),
                "El código en claro no debe aparecer en ninguna columna");
    }

    @Test
    @DisplayName("Leído de la base, el caso solo se cierra con su código")
    void laHuellaSobreviveALaBase() {
        // Arrange
        ReportePublicado publicado = gestion.publicar(TipoReporte.PERDIDA, solicitud());
        String id = publicado.reporte().getId();
        // Act y Assert
        assertThrows(CodigoGestionInvalidoException.class,
                () -> gestion.cerrar(id, CodigoGestion.generar().valor()));
        gestion.cerrar(id, publicado.codigoGestion());
        assertEquals(EstadoReporte.CERRADO, gestion.consultar(id).getEstado());
    }
}
