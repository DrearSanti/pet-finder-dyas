package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

/**
 * /actuator/info dice qué commit y qué versión están corriendo.
 *
 * El despliegue a Render depende de esto: espera a que la URL muestre el SHA
 * recién publicado antes de correr la prueba de humo y la de carga. Si /info
 * dejara de exponer el commit, el pipeline esperaría para siempre o, peor,
 * probaría la versión anterior y saldría en verde.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false",
                "PETFINDER_COMMIT=0123456789abcdef", "PETFINDER_VERSION=v9.9.9"})
class VersionDesplegadaSistemaIT {

    @LocalServerPort
    private int puerto;

    @Test
    @DisplayName("/actuator/info muestra el commit y la versión que recibió la imagen")
    @SuppressWarnings("unchecked")
    void infoMuestraCommitYVersion() {
        // Arrange
        RestClient http = RestClient.create("http://localhost:" + puerto);
        // Act
        Map<String, Object> info = http.get().uri("/actuator/info").retrieve().body(Map.class);
        // Assert
        Map<String, Object> app = (Map<String, Object>) info.get("app");
        assertEquals("0123456789abcdef", app.get("commit"));
        assertEquals("v9.9.9", app.get("version"));
    }
}
