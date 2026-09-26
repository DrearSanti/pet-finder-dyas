package petfinder.integracion.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * Prueba de sistema de caja negra: la aplicación completa (controlador →
 * puerto → servicio → dominio → H2) levantada en un puerto real, y solo HTTP
 * desde afuera, como la usaría el navegador.
 *
 * El cliente es {@link RestClient} de Spring Web, configurado para no lanzar
 * excepciones con 4xx: aquí un 400 o un 409 es un resultado que se afirma, no
 * un fallo. La base no se revierte entre pruebas (el servidor atiende en otro
 * hilo), así que ninguna prueba supone un id fijo: cada una usa el id que le
 * devolvió su propio POST.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class FlujoReportePerdidaSistemaIT {

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

    /** Cuerpo válido de una pérdida; cada prueba cambia solo lo que quiere probar. */
    private static Map<String, Object> perdidaValida() {
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("nombre", "Luna");
        mascota.put("especie", "perro");
        mascota.put("raza", "criolla");
        mascota.put("color", "blanca");
        mascota.put("senas", "mancha negra en la oreja");

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipo", "PERDIDA");
        cuerpo.put("zona", "Cedritos");
        cuerpo.put("referencia", "Parque de la 147");
        cuerpo.put("latitud", 4.7235);
        cuerpo.put("longitud", -74.0417);
        cuerpo.put("descripcion", "Se escapó en la tarde");
        cuerpo.put("nombreContacto", "Camila");
        cuerpo.put("medioContacto", "3001234567");
        cuerpo.put("mascota", mascota);
        return cuerpo;
    }

    private static Map<String, Object> avistamiento() {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("zona", "Cedritos");
        cuerpo.put("referencia", "Canchas");
        cuerpo.put("descripcion", "La vi corriendo hacia el parque");
        return cuerpo;
    }

    private String crearPerdida() {
        ResponseEntity<Map> creado = post("/api/reportes", perdidaValida());
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        return (String) creado.getBody().get("id");
    }

    @Test
    @DisplayName("Ciclo completo por HTTP: crear, avistar, resolver y rechazar un avistamiento tardío")
    void cicloCompletoDeUnaPerdida() {
        // Arrange
        ResponseEntity<Map> creado = post("/api/reportes", perdidaValida());
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        String id = (String) creado.getBody().get("id");
        assertNotNull(id);
        assertEquals("ACTIVO", creado.getBody().get("estado"));

        // Act
        ResponseEntity<Map> conPista = post("/api/reportes/" + id + "/avistamientos", avistamiento());
        ResponseEntity<Map> resuelto = post("/api/reportes/" + id + "/resolver", null);
        ResponseEntity<Map> consultado = get("/api/reportes/" + id);
        ResponseEntity<Map> pistaTardia = post("/api/reportes/" + id + "/avistamientos", avistamiento());

        // Assert
        assertEquals(HttpStatus.CREATED, conPista.getStatusCode());
        assertEquals(1, conPista.getBody().get("cantidadAvistamientos"));
        assertEquals(HttpStatus.NO_CONTENT, resuelto.getStatusCode());
        assertEquals(HttpStatus.OK, consultado.getStatusCode());
        assertEquals("RESUELTO", consultado.getBody().get("estado"));
        assertEquals(HttpStatus.CONFLICT, pistaTardia.getStatusCode());
        assertNotNull(pistaTardia.getBody().get("error"));
    }

    @Test
    @DisplayName("El detalle muestra el contacto completo y la lista pública lo enmascara")
    void listaEnmascaraYDetalleNo() {
        // Arrange
        String id = crearPerdida();

        // Act
        ResponseEntity<Map> detalle = get("/api/reportes/" + id);
        Map<String, Object> enLista = activos().stream()
                .filter(reporte -> id.equals(reporte.get("id")))
                .findFirst().orElseThrow();

        // Assert
        assertEquals("3001234567", detalle.getBody().get("contacto"));
        assertEquals("300•••••67", enLista.get("contacto"));
        assertEquals(4.724, enLista.get("latitud"));
    }

    @Test
    @DisplayName("Un reporte sin zona responde 400 con el mensaje del dominio y no se crea")
    void sinZonaResponde400() {
        // Arrange
        Map<String, Object> sinZona = perdidaValida();
        sinZona.remove("zona");
        int antes = activos().size();

        // Act
        ResponseEntity<Map> respuesta = post("/api/reportes", sinZona);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Se requiere la zona o barrio donde ocurrió el hecho", respuesta.getBody().get("error"));
        assertEquals(antes, activos().size());
    }

    @Test
    @DisplayName("Un JSON ilegible responde 400 con un mensaje para la persona")
    void jsonIlegibleResponde400() {
        // Act
        ResponseEntity<Map> respuesta = post("/api/reportes", "{ esto no es json");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El cuerpo de la petición no es válido", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Consultar PF-999999 responde 404")
    void idInexistenteResponde404() {
        // Act
        ResponseEntity<Map> respuesta = get("/api/reportes/PF-999999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("No existe un reporte con el identificador PF-999999", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Resolver dos veces el mismo reporte responde 409 la segunda vez")
    void resolverDosVecesResponde409() {
        // Arrange
        String id = crearPerdida();

        // Act
        ResponseEntity<Map> primera = post("/api/reportes/" + id + "/resolver", null);
        ResponseEntity<Map> segunda = post("/api/reportes/" + id + "/resolver", null);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, primera.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, segunda.getStatusCode());
        assertTrue(((String) segunda.getBody().get("error")).contains(id));
    }
}
