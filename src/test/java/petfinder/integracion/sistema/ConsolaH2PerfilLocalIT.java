package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

/**
 * El perfil local sigue sirviendo para lo que existía la consola: mirar las
 * tablas mientras se desarrolla. Si alguien borra application-local.properties
 * o le cambia el nombre, el README dejaría de ser cierto y esta prueba falla.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
@ActiveProfiles("local")
class ConsolaH2PerfilLocalIT {

    @LocalServerPort
    private int puerto;

    @Test
    @DisplayName("Con el perfil local, /h2-console/ responde 200")
    void consolaPrendidaEnPerfilLocal() {
        // Arrange
        RestClient http = RestClient.create("http://localhost:" + puerto);
        // Act
        ResponseEntity<String> respuesta = http.get().uri("/h2-console/")
                .retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(String.class);
        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    }
}
