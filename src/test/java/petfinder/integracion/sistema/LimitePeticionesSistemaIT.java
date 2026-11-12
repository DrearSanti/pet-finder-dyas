package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Prueba de sistema del límite de peticiones (S10), con el servidor real.
 *
 * Cubre las amenazas T2, T5 y T7: antes de S10, 3000 llamadas seguidas al
 * asistente daban 3000 respuestas 200. La IP va en CF-Connecting-IP y es
 * aleatoria, como la que escribe Cloudflare, para no compartir cubeta con
 * otras pruebas. El global del asistente se sube aquí: con propiedades
 * propias esta clase tiene su propio contexto y su propio contador, así otra
 * prueba que llame al asistente no la vuelve inestable.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "ANTHROPIC_API_KEY=",
        "petfinder.datos-ejemplo=false",
        "petfinder.limite.asistente.por-ip=10",
        "petfinder.limite.asistente.global=1000"})
class LimitePeticionesSistemaIT {

    @LocalServerPort
    private int puerto;

    private RestClient http;

    @BeforeEach
    void conectar() {
        http = RestClient.create("http://localhost:" + puerto);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> turno(String ip) {
        return http.post().uri("/api/asistente/turno").contentType(MediaType.APPLICATION_JSON)
                .header("CF-Connecting-IP", ip)
                .body("{\"texto\":\"perdí un perro llamado Max en Chía\"}")
                .retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(Map.class);
    }

    private static String ipAleatoria() {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        return "198.18." + azar.nextInt(256) + "." + azar.nextInt(1, 255);
    }

    @Test
    @DisplayName("Diez turnos del asistente por minuto pasan y el once responde 429 con Retry-After")
    void laOnceResponde429() {
        // Arrange
        String ip = ipAleatoria();
        for (int i = 1; i <= 10; i++) {
            assertEquals(HttpStatus.OK, turno(ip).getStatusCode(), "La llamada " + i + " debe pasar");
        }
        // Act
        ResponseEntity<Map> once = turno(ip);
        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, once.getStatusCode());
        assertEquals("60", once.getHeaders().getFirst("Retry-After"));
        assertEquals("Demasiadas peticiones. Espera un minuto e intenta de nuevo.", once.getBody().get("error"));
    }

    @Test
    @DisplayName("Otra IP sigue pudiendo usar el asistente aunque una ya esté limitada")
    void otraIpNoQuedaLimitada() {
        // Arrange
        String limitada = ipAleatoria();
        for (int i = 0; i < 11; i++) {
            turno(limitada);
        }
        // Act
        ResponseEntity<Map> otra = turno("198.19.0." + ThreadLocalRandom.current().nextInt(1, 255));
        // Assert
        assertEquals(HttpStatus.OK, otra.getStatusCode());
    }
}
