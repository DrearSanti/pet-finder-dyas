package petfinder.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Los dos flujos principales de la interfaz, en un Chrome real sin ventana.
 *
 * Solo corre con {@code ./mvnw -q -Pui verify}: necesita Chrome instalado
 * (Selenium Manager descarga el driver que corresponde), y así las máquinas
 * sin Chrome pasan el gate de siempre. La app arranca completa en un puerto
 * aleatorio y sin clave de Anthropic, de modo que el asistente usa el
 * extractor por reglas y nunca llama a Claude.
 *
 * Cada prueba abre su propio navegador y crea sus propios datos con un nombre
 * de mascota al azar: no depende de los datos de ejemplo ni de la otra prueba.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"ANTHROPIC_API_KEY=", "petfinder.datos-ejemplo=false"})
class FlujosPrincipalesUIT {

    @LocalServerPort
    private int puerto;

    private WebDriver navegador;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions opciones = new ChromeOptions();
        // Ancho de escritorio: la lista de casos y el panel se ven a la vez (DESIGN.md §10).
        opciones.addArguments("--headless=new", "--window-size=1440,1000");
        navegador = new ChromeDriver(opciones);
    }

    @AfterEach
    void cerrarNavegador() {
        if (navegador != null) {
            navegador.quit();
        }
    }

    private String urlBase() {
        return "http://localhost:" + puerto;
    }

    /** Un nombre que ninguna otra prueba ni dato de ejemplo usa, para encontrar el caso sin suponer su id. */
    private static String nombreUnico() {
        StringBuilder nombre = new StringBuilder("Z");
        for (int i = 0; i < 6; i++) {
            nombre.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return nombre.toString();
    }

    @Test
    @DisplayName("Escribir el reporte, completar el contacto y publicar lo muestra en la lista de casos")
    void publicarUnReporteEscrito() {
        // Arrange
        String nombre = nombreUnico();
        PaginaInicio inicio = new PaginaInicio(navegador).abrir(urlBase());

        // Act
        PaginaNuevoReporte nuevo = inicio.irANuevoReporte()
                .escribir("perdí un perro llamado " + nombre + " en Cedritos")
                .esperarCampo("nombre", nombre)
                .esperarCampo("zona", "Cedritos")
                .completarContacto("3001234567");
        String id = nuevo.publicar().esperarCasoQueDiga(nombre);

        // Assert
        assertNotNull(id);
        assertTrue(id.startsWith("PF-"), "El caso publicado debe tener un id de reporte: " + id);
    }

    @Test
    @DisplayName("Abrir un caso, tocar La vi y enviar la pista muestra la confirmación")
    void reportarUnAvistamiento() {
        // Arrange: el caso se crea por la API; lo que se prueba es la interfaz del avistamiento
        String nombre = nombreUnico();
        String id = crearPerdidaPorApi(nombre);
        PaginaInicio inicio = new PaginaInicio(navegador).abrir(urlBase());

        // Act
        String mensaje = inicio.abrirCaso(id)
                .pulsarLaVi()
                .enviarPista()
                .esperarMensajeQueDiga("Pista enviada");

        // Assert
        assertTrue(mensaje.contains("Pista enviada. Gracias por avisar."), mensaje);
    }

    @SuppressWarnings("unchecked")
    private String crearPerdidaPorApi(String nombre) {
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("nombre", nombre);
        mascota.put("especie", "perro");
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipo", "PERDIDA");
        cuerpo.put("zona", "Cedritos");
        cuerpo.put("descripcion", "Se escapó en el parque");
        cuerpo.put("nombreContacto", "Camila");
        cuerpo.put("medioContacto", "3001234567");
        cuerpo.put("mascota", mascota);
        Map<String, Object> creado = RestClient.create(urlBase()).post().uri("/api/reportes")
                .contentType(MediaType.APPLICATION_JSON).body(cuerpo)
                .retrieve().body(Map.class);
        return (String) creado.get("id");
    }
}
