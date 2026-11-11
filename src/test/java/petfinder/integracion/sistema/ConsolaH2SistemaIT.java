package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * La consola de H2 no existe en la configuración con la que se despliega.
 *
 * En Render respondía 200 a cualquiera: no aceptaba conexiones remotas, pero
 * mostraba que el sistema usa H2 y era una puerta más hacia la base. Esta
 * prueba arranca la app sin perfil, igual que Render y Docker, y espera 404.
 * {@link ConsolaH2PerfilLocalIT} comprueba que en el perfil local sí se prende.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class ConsolaH2SistemaIT {

    @LocalServerPort
    private int puerto;

    @Test
    @DisplayName("Sin perfil, /h2-console/ responde 404")
    void consolaApagadaPorDefecto() {
        // Arrange
        RestClient http = RestClient.create("http://localhost:" + puerto);
        // Act
        ResponseEntity<String> respuesta = http.get().uri("/h2-console/")
                .retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(String.class);
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }
}
