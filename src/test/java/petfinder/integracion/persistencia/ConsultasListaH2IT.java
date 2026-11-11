package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManagerFactory;
import petfinder.application.port.salida.RepositorioReportes;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.Ubicacion;

/**
 * La lista de casos activos sale en una sola consulta y con tope (S8).
 *
 * Antes eran 1 + N consultas: una por los reportes y otra por los
 * avistamientos de cada uno. Hibernate cuenta las sentencias preparadas
 * (generate_statistics), así que la prueba mide lo mismo que se vería con
 * show-sql. El tope se baja a 2 para comprobarlo con pocos datos; las
 * propiedades propias le dan a esta clase su contexto y su base.
 */
@SpringBootTest(properties = {
        "ANTHROPIC_API_KEY=",
        "petfinder.datos-ejemplo=false",
        "petfinder.lista.maximo=2",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
class ConsultasListaH2IT {

    @Autowired
    private RepositorioReportes repositorio;

    @Autowired
    private EntityManagerFactory fabrica;

    @Test
    @DisplayName("Listar los activos con sus avistamientos hace una sola consulta")
    void unaSolaConsulta() {
        // Arrange
        LocalDateTime base = LocalDateTime.now().plusYears(1);
        for (int i = 0; i < 3; i++) {
            repositorio.guardar(perdidaConAvistamientos(idNuevo(), base.plusMinutes(i)));
        }
        Statistics estadisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();

        // Act
        repositorio.listarActivos();

        // Assert
        assertEquals(1, estadisticas.getPrepareStatementCount(),
                "La lista debe salir en una sola consulta, con los avistamientos incluidos");
    }

    @Test
    @DisplayName("La lista devuelve solo los casos activos más recientes, hasta el tope")
    void respetaElTopeYElOrden() {
        // Arrange: tres casos más recientes que cualquier otro de esta base
        LocalDateTime base = LocalDateTime.now().plusYears(2);
        String antiguo = idNuevo();
        String medio = idNuevo();
        String reciente = idNuevo();
        repositorio.guardar(perdidaConAvistamientos(antiguo, base));
        repositorio.guardar(perdidaConAvistamientos(medio, base.plusMinutes(1)));
        repositorio.guardar(perdidaConAvistamientos(reciente, base.plusMinutes(2)));

        // Act
        List<ReporteMascota> activos = repositorio.listarActivos();

        // Assert
        assertEquals(List.of(reciente, medio), activos.stream().map(ReporteMascota::getId).toList());
        activos.forEach(r -> assertEquals(2, ((ReportePerdida) r).getAvistamientos().size(),
                "Cada caso llega con sus dos avistamientos, sin repetirse por el join"));
    }

    private static String idNuevo() {
        return "PF-L" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static ReportePerdida perdidaConAvistamientos(String id, LocalDateTime fecha) {
        Ubicacion ubicacion = new Ubicacion("Cedritos", "Parque 147", 4.7235, -74.0417);
        Contacto vecino = new Contacto("Vecino", "3009998877");
        List<Avistamiento> avistamientos = List.of(
                new Avistamiento(id + "-A1", fecha.plusSeconds(10), ubicacion, "La vi en el parque", vecino),
                new Avistamiento(id + "-A2", fecha.plusSeconds(20), ubicacion, "Iba hacia la 147", vecino));
        return ReportePerdida.reconstruir(id, ubicacion, "Se escapó en la tarde",
                new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"), fecha, EstadoReporte.ACTIVO, avistamientos);
    }
}
