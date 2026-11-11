package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Las cabeceras llegan por HTTP real, y la página cumple la política que ella
 * misma recibe: sin scripts dentro del HTML y con Leaflet verificado por hash.
 * Si alguien agrega un script en línea, el navegador lo bloquearía en silencio
 * y la función se rompería sin que ninguna otra prueba lo note.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class CabecerasSeguridadSistemaIT {

    private static final Pattern SCRIPT_EN_LINEA = Pattern.compile("<script(?![^>]*\\bsrc=)[^>]*>");

    @LocalServerPort
    private int puerto;

    private RestClient http;

    @BeforeEach
    void conectar() {
        http = RestClient.create("http://localhost:" + puerto);
    }

    private ResponseEntity<String> get(String ruta) {
        return http.get().uri(ruta).retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(String.class);
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("La página, la API y un 404 traen todas las cabeceras de seguridad")
    @ValueSource(strings = {"/", "/api/reportes", "/no-existe"})
    void todasLasRespuestasTraenCabeceras(String ruta) {
        // Act
        HttpHeaders cabeceras = get(ruta).getHeaders();
        // Assert
        String politica = cabeceras.getFirst("Content-Security-Policy");
        assertNotNull(politica);
        assertTrue(politica.contains("frame-ancestors 'none'"));
        assertFalse(politica.contains("unsafe-inline"));
        assertEquals("nosniff", cabeceras.getFirst("X-Content-Type-Options"));
        assertNotNull(cabeceras.getFirst("Strict-Transport-Security"));
        assertTrue(cabeceras.getFirst("Permissions-Policy").contains("microphone=(self)"));
        assertEquals("strict-origin-when-cross-origin", cabeceras.getFirst("Referrer-Policy"));
    }

    @Test
    @DisplayName("La página no tiene scripts en línea y Leaflet lleva integrity")
    void paginaCumpleLaPolitica() {
        // Act
        String pagina = get("/").getBody();
        // Assert
        assertFalse(SCRIPT_EN_LINEA.matcher(pagina).find());
        assertTrue(pagina.contains("leaflet.min.js\" integrity=\"sha512-"));
        assertTrue(pagina.contains("leaflet.min.css\" integrity=\"sha512-"));
    }

    @Test
    @DisplayName("El registro del service worker se sirve como archivo propio")
    void registroServiceWorkerExiste() {
        // Act
        ResponseEntity<String> respuesta = get("/js/registro-sw.js");
        // Assert
        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("serviceWorker.register"));
    }
}
