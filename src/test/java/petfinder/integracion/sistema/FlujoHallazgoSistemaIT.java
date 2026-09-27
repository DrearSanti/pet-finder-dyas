package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
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
 * "La tengo yo" de punta a punta, solo por HTTP: quien encontró a la mascota
 * avisa en el caso de la familia, el caso lo muestra (con su contacto) y la
 * familia lo cierra cuando la recoge. Pasa por el controlador, el servicio,
 * el dominio y H2, así que también prueba que el tipo se guarda y se lee.
 * Como en FlujoReportePerdidaSistemaIT, ninguna prueba supone un id fijo.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class FlujoHallazgoSistemaIT {

    @LocalServerPort
    private int puerto;

    private RestClient http;

    @BeforeEach
    void conectar() {
        http = RestClient.create("http://localhost:" + puerto);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> post(String ruta, Object cuerpo) {
        RestClient.RequestBodySpec peticion = http.post().uri(ruta).contentType(MediaType.APPLICATION_JSON);
        if (cuerpo != null) {
            peticion.body(cuerpo);
        }
        return peticion.retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(Map.class);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> get(String ruta) {
        return http.get().uri(ruta).retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> activos() {
        return http.get().uri("/api/reportes").retrieve().body(List.class);
    }

    @SuppressWarnings("rawtypes")
    private String crearPerdidaDeCopito() {
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("nombre", "Copito");
        mascota.put("especie", "perro");
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipo", "PERDIDA");
        cuerpo.put("zona", "Usaquén");
        cuerpo.put("descripcion", "Se escapó del conjunto");
        cuerpo.put("nombreContacto", "Camila");
        cuerpo.put("medioContacto", "3001234567");
        cuerpo.put("mascota", mascota);
        ResponseEntity<Map> creado = post("/api/reportes", cuerpo);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        return (String) creado.getBody().get("id");
    }

    private static Map<String, Object> laTengo(String medioContacto) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipo", "LA_TENGO");
        cuerpo.put("zona", "Palatino");
        cuerpo.put("descripcion", "Está en mi casa, tranquilo");
        cuerpo.put("nombreContacto", "Andrés");
        cuerpo.put("medioContacto", medioContacto);
        return cuerpo;
    }

    private static Map<String, Object> caso(List<Map<String, Object>> casos, String id) {
        return casos.stream().filter(c -> id.equals(c.get("id"))).findFirst().orElse(null);
    }

    @Test
    @DisplayName("La tengo yo: el caso muestra quién la tiene y su contacto, y la familia lo cierra al recogerla")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void hallazgoAsociadoAlCaso() {
        // Arrange
        String id = crearPerdidaDeCopito();

        // Act
        ResponseEntity<Map> avisado = post("/api/reportes/" + id + "/avistamientos", laTengo("3109876543"));
        ResponseEntity<Map> detalle = get("/api/reportes/" + id);
        Map<String, Object> enLista = caso(activos(), id);
        ResponseEntity<Map> resuelto = post("/api/reportes/" + id + "/resolver", null);

        // Assert
        assertEquals(HttpStatus.CREATED, avisado.getStatusCode());
        assertEquals(Boolean.TRUE, detalle.getBody().get("laTieneAlguien"));
        Map<String, Object> aviso = ((List<Map<String, Object>>) detalle.getBody().get("avistamientos")).get(0);
        assertEquals("LA_TENGO", aviso.get("tipo"));
        assertNull(aviso.get("contacto"));
        Map<String, Object> hallazgo = (Map<String, Object>) detalle.getBody().get("hallazgo");
        assertEquals("Palatino", hallazgo.get("zona"));
        assertEquals("Andrés", hallazgo.get("nombreContacto"));
        assertEquals("3109876543", hallazgo.get("contacto"));
        assertNull(enLista.get("hallazgo"));
        assertEquals(Boolean.TRUE, enLista.get("laTieneAlguien"));
        assertEquals("ACTIVO", enLista.get("estado"));
        assertEquals(HttpStatus.NO_CONTENT, resuelto.getStatusCode());
        assertNull(caso(activos(), id));
    }

    @Test
    @DisplayName("La tengo yo sin contacto responde 400 con el mensaje del dominio y no toca el caso")
    @SuppressWarnings("rawtypes")
    void hallazgoSinContactoResponde400() {
        // Arrange
        String id = crearPerdidaDeCopito();

        // Act
        ResponseEntity<Map> rechazado = post("/api/reportes/" + id + "/avistamientos", laTengo(""));
        ResponseEntity<Map> detalle = get("/api/reportes/" + id);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, rechazado.getStatusCode());
        assertEquals("Si la tienes contigo, deja tu contacto para que su familia te encuentre.",
                rechazado.getBody().get("error"));
        assertEquals(Boolean.FALSE, detalle.getBody().get("laTieneAlguien"));
        assertEquals(0, detalle.getBody().get("cantidadAvistamientos"));
    }

    @Test
    @DisplayName("Una pista (La vi) sigue sin mostrar el contacto de quien la dejó ni cuenta como hallazgo")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void pistaNoMuestraContacto() {
        // Arrange
        String id = crearPerdidaDeCopito();
        Map<String, Object> pista = laTengo("3109876543");
        pista.put("tipo", "LA_VI");

        // Act
        post("/api/reportes/" + id + "/avistamientos", pista);
        ResponseEntity<Map> detalle = get("/api/reportes/" + id);

        // Assert
        Map<String, Object> dejada = ((List<Map<String, Object>>) detalle.getBody().get("avistamientos")).get(0);
        assertEquals("LA_VI", dejada.get("tipo"));
        assertNull(dejada.get("contacto"));
        assertNull(detalle.getBody().get("hallazgo"));
        assertFalse((Boolean) detalle.getBody().get("laTieneAlguien"));
        assertTrue(((List<?>) detalle.getBody().get("avistamientos")).size() == 1);
    }
}
