package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
 * Prueba de sistema de los topes de entrada, con el servidor y la base reales.
 *
 * Es la verificación de la amenaza de denegación de servicio del modelo de
 * amenazas: antes, 100 KB al asistente ocupaban un núcleo 17 segundos, un
 * cuerpo enorme podía agotar la memoria y un campo largo terminaba en un 500.
 * Ahora la frase larga y el campo largo responden 400 y el cuerpo enorme 413,
 * los tres al instante. Sin clave de Anthropic, así que nunca llama a Claude.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class LimitesEntradaSistemaIT {

    @LocalServerPort
    private int puerto;

    private RestClient http;

    @BeforeEach
    void conectar() {
        http = RestClient.create("http://localhost:" + puerto);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> post(String ruta, String cuerpo) {
        return http.post().uri(ruta).contentType(MediaType.APPLICATION_JSON).body(cuerpo)
                .retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(Map.class);
    }

    @Test
    @DisplayName("Una frase de 2 KB al asistente responde 400 con el tope en el mensaje")
    void fraseDeDosKBResponde400() {
        // Arrange
        String cuerpo = "{\"texto\":\"" + "a".repeat(2 * 1024) + "\"}";
        // Act
        ResponseEntity<Map> respuesta = post("/api/asistente/turno", cuerpo);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(((String) respuesta.getBody().get("error")).contains("1000"));
    }

    @Test
    @DisplayName("Un cuerpo de 1 MB al asistente responde 413")
    void cuerpoDeUnMBResponde413() {
        // Arrange
        String cuerpo = "{\"texto\":\"" + "a".repeat(1024 * 1024) + "\"}";
        // Act
        ResponseEntity<Map> respuesta = post("/api/asistente/turno", cuerpo);
        // Assert
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, respuesta.getStatusCode());
        assertEquals("La petición es demasiado grande", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Un cuerpo de 100 KB enviado por partes, sin declarar su tamaño, también responde 413")
    void cuerpoPorPartesResponde413() throws Exception {
        // Arrange: un flujo sin tamaño conocido obliga al cliente a mandarlo por partes (chunked)
        byte[] cuerpo = ("{\"texto\":\"" + "a".repeat(100 * 1024) + "\"}").getBytes(StandardCharsets.UTF_8);
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/asistente/turno"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(cuerpo)))
                .build();
        HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        // Act
        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        // Assert
        assertEquals(413, respuesta.statusCode());
    }

    @Test
    @DisplayName("Un reporte con una zona de 300 caracteres responde 400 y no 500")
    void campoLargoResponde400() {
        // Arrange
        String cuerpo = """
                { "tipo": "ENCONTRADA", "zona": "%s", "descripcion": "Perro café con collar rojo",
                  "nombreContacto": "Ana", "medioContacto": "3001112233",
                  "descripcionMascota": "Perro mediano café" }
                """.formatted("z".repeat(300));
        // Act
        ResponseEntity<Map> respuesta = post("/api/reportes", cuerpo);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El campo zona admite hasta 255 caracteres", respuesta.getBody().get("error"));
    }
}
