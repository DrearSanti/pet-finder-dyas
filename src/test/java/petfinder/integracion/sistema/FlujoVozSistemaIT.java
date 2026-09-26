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
 * Prueba de sistema de los canales de voz y asistente, solo por HTTP.
 *
 * {@link #vozFormularioYAsistenteProducenElMismoReporte} es la evidencia del
 * reto de modificabilidad: tres entradas distintas (formulario, comando de voz
 * y borrador del asistente) llegan al mismo caso de uso y producen el mismo
 * reporte. Sin clave de Anthropic el asistente usa el extractor por reglas, así
 * que la prueba nunca llama a Claude.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class FlujoVozSistemaIT {

    private static final String FRASE = "perdí un perro llamado Max en Chía";

    @LocalServerPort
    private int puerto;

    private RestClient http;

    @BeforeEach
    void conectar() {
        http = RestClient.create("http://localhost:" + puerto);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> post(String ruta, Object cuerpo) {
        return http.post().uri(ruta).contentType(MediaType.APPLICATION_JSON).body(cuerpo)
                .retrieve().onStatus(estado -> true, (req, res) -> { }).toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> activos() {
        return http.get().uri("/api/reportes").retrieve().body(List.class);
    }

    private static Map<String, Object> comandoVoz(String texto) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("texto", texto);
        cuerpo.put("nombreContacto", "Ana");
        cuerpo.put("medioContacto", "3001112233");
        return cuerpo;
    }

    @SuppressWarnings("unchecked")
    private static String idCreadoPorVoz(ResponseEntity<Map> respuesta) {
        List<Map<String, Object>> reportes = (List<Map<String, Object>>) respuesta.getBody().get("reportes");
        return (String) reportes.get(0).get("id");
    }

    @Test
    @DisplayName("Un comando de voz de pérdida responde 201 y el caso aparece en la lista")
    void vozCreaUnReporteVisibleEnLaLista() {
        // Act
        ResponseEntity<Map> respuesta = post("/api/voz", comandoVoz(FRASE));

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals("REGISTRAR_PERDIDA", respuesta.getBody().get("accion"));
        String id = idCreadoPorVoz(respuesta);
        assertNotNull(id);
        assertTrue(activos().stream().anyMatch(reporte -> id.equals(reporte.get("id"))));
    }

    @Test
    @DisplayName("Una frase ininteligible responde 422 con ayuda y no crea nada")
    void fraseIninteligibleResponde422() {
        // Arrange
        int antes = activos().size();

        // Act
        ResponseEntity<Map> respuesta = post("/api/voz", comandoVoz("el clima está bonito hoy en la tarde"));

        // Assert
        assertEquals(422, respuesta.getStatusCode().value());
        assertEquals("No entendí. Prueba con: perdí un perro llamado Max en Chía", respuesta.getBody().get("error"));
        assertEquals(antes, activos().size());
    }

    @Test
    @DisplayName("Listar por voz responde 200 sin crear reportes")
    void listarPorVozResponde200() {
        // Arrange
        int antes = activos().size();

        // Act
        ResponseEntity<Map> respuesta = post("/api/voz", comandoVoz("ver reportes activos"));

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("LISTAR_ACTIVOS", respuesta.getBody().get("accion"));
        assertEquals(antes, activos().size());
    }

    @Test
    @DisplayName("Formulario, voz y asistente producen el mismo tipo, nombre, especie y zona")
    @SuppressWarnings("unchecked")
    void vozFormularioYAsistenteProducenElMismoReporte() {
        // Arrange: el formulario, tal como lo envía la página
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("nombre", "Max");
        mascota.put("especie", "perro");
        Map<String, Object> formulario = new HashMap<>();
        formulario.put("tipo", "PERDIDA");
        formulario.put("zona", "Chía");
        formulario.put("descripcion", FRASE);
        formulario.put("nombreContacto", "Ana");
        formulario.put("medioContacto", "3001112233");
        formulario.put("mascota", mascota);
        Map<String, Object> turno = new HashMap<>();
        turno.put("borrador", null);
        turno.put("texto", FRASE + ", mi número es 300 111 2233");

        // Act 1: formulario
        ResponseEntity<Map> porFormulario = post("/api/reportes", formulario);

        // Act 2: comando de voz
        ResponseEntity<Map> porVoz = post("/api/voz", comandoVoz(FRASE));
        Map<String, Object> reporteVoz = http.get().uri("/api/reportes/" + idCreadoPorVoz(porVoz))
                .retrieve().body(Map.class);

        // Act 3: el asistente arma el borrador y la persona lo publica con el mismo POST del formulario
        ResponseEntity<Map> respuestaTurno = post("/api/asistente/turno", turno);
        Map<String, Object> borrador = (Map<String, Object>) respuestaTurno.getBody().get("borrador");
        ResponseEntity<Map> porAsistente = post("/api/reportes", desdeBorrador(borrador));

        // Assert
        assertEquals(HttpStatus.CREATED, porFormulario.getStatusCode());
        assertEquals(HttpStatus.CREATED, porVoz.getStatusCode());
        assertEquals(true, respuestaTurno.getBody().get("listo"));
        assertEquals(HttpStatus.CREATED, porAsistente.getStatusCode());
        Map<String, Object> esperado = porFormulario.getBody();
        for (Map<String, Object> reporte : List.of(reporteVoz, (Map<String, Object>) porAsistente.getBody())) {
            assertEquals(esperado.get("tipo"), reporte.get("tipo"));
            assertEquals(esperado.get("nombreMascota"), reporte.get("nombreMascota"));
            assertEquals(esperado.get("especie"), reporte.get("especie"));
            assertEquals(esperado.get("zona"), reporte.get("zona"));
        }
    }

    /** Lo que hace la página al tocar "Publicar reporte": la tarjeta viva pasa a ser el cuerpo del formulario. */
    private static Map<String, Object> desdeBorrador(Map<String, Object> borrador) {
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("nombre", borrador.get("nombre"));
        mascota.put("especie", borrador.get("especie"));
        mascota.put("raza", borrador.get("raza"));
        mascota.put("color", borrador.get("color"));
        mascota.put("senas", borrador.get("senas"));
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipo", borrador.get("tipo"));
        cuerpo.put("zona", borrador.get("zona"));
        cuerpo.put("referencia", borrador.get("referencia"));
        cuerpo.put("latitud", borrador.get("latitud"));
        cuerpo.put("longitud", borrador.get("longitud"));
        cuerpo.put("descripcion", borrador.get("descripcion"));
        cuerpo.put("nombreContacto", borrador.get("contactoNombre"));
        cuerpo.put("medioContacto", borrador.get("contactoMedio"));
        cuerpo.put("mascota", mascota);
        cuerpo.put("descripcionMascota", borrador.get("descripcionMascota"));
        return cuerpo;
    }
}
