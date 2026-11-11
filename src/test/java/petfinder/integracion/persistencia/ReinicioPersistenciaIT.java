package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import petfinder.PetFinderApplication;
import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Simula un reinicio contra una base que sobrevive, como PostgreSQL en Compose:
 * la aplicación arranca dos veces seguidas contra la misma base H2 con nombre
 * (DB_CLOSE_DELAY=-1 la mantiene viva entre los dos arranques). Antes de S7 el
 * segundo arranque volvía a PF-001 y el siguiente POST pisaba el caso guardado.
 */
class ReinicioPersistenciaIT {

    @Test
    @DisplayName("Tras reiniciar, el caso sigue ahí, el siguiente ID es nuevo y la lista crece en 1")
    void elCasoSobreviveAlReinicio() {
        // Arrange
        String base = "--spring.datasource.url=jdbc:h2:mem:reinicio-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        String idAntes;
        int activosAntes;
        try (ConfigurableApplicationContext app = arrancar(base)) {
            GestionReportes gestion = app.getBean(GestionReportes.class);
            idAntes = gestion.registrar(TipoReporte.PERDIDA, perdida("Toby")).getId();
            activosAntes = gestion.listarActivos().size();
        }

        // Act
        try (ConfigurableApplicationContext app = arrancar(base)) {
            GestionReportes gestion = app.getBean(GestionReportes.class);
            ReportePerdida guardado = (ReportePerdida) gestion.consultar(idAntes);
            String idNuevo = gestion.registrar(TipoReporte.PERDIDA, perdida("Nala")).getId();
            int activosDespues = gestion.listarActivos().size();

            // Assert
            assertEquals("Toby", guardado.getMascota().nombre());
            assertTrue(GeneradorIdReportes.numeroDe(idNuevo) > GeneradorIdReportes.numeroDe(idAntes),
                    "El ID después del reinicio debe ser nuevo: antes " + idAntes + ", después " + idNuevo);
            assertEquals(activosAntes + 1, activosDespues,
                    "Solo debe sumarse Nala: los casos de ejemplo no se cargan otra vez");
        }
    }

    private static ConfigurableApplicationContext arrancar(String base) {
        // Argumentos de línea de comandos: ganan sobre application.properties y sobre el .env local.
        return new SpringApplicationBuilder(PetFinderApplication.class)
                .run("--server.port=0", "--ANTHROPIC_API_KEY=", base);
    }

    private static SolicitudReporte perdida(String nombre) {
        return SolicitudReporte.paraPerdida(
                new Ubicacion("Chapinero", "Parque de los Hippies", 4.6486, -74.0628),
                "Salió por la puerta del garaje.",
                new Contacto("Prueba de reinicio", "3001112233"),
                new Mascota(nombre, "perro", "criollo", "negro", "collar verde"));
    }
}
