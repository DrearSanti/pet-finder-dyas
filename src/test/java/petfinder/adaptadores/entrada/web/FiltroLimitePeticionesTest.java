package petfinder.adaptadores.entrada.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import petfinder.adaptadores.entrada.web.FiltroLimitePeticiones.Limites;

/**
 * El filtro de límite con un reloj falso: el tiempo avanza cuando la prueba
 * lo dice, así que nada depende de esperar ni de la velocidad de la máquina.
 */
class FiltroLimitePeticionesTest {

    private static final long UN_SEGUNDO = 1_000_000_000L;

    private final AtomicLong reloj = new AtomicLong();

    private FiltroLimitePeticiones filtro(int asistentePorIp, int asistenteGlobal) {
        return new FiltroLimitePeticiones(new Limites(asistentePorIp, asistenteGlobal, 5, 5), reloj::get);
    }

    private static int estado(FiltroLimitePeticiones filtro, String ruta, String ip) throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", ruta);
        peticion.setRequestURI(ruta);
        if (ip != null) {
            peticion.addHeader("CF-Connecting-IP", ip);
        }
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        filtro.doFilter(peticion, respuesta, new MockFilterChain());
        return respuesta.getStatus();
    }

    @Test
    @DisplayName("La llamada 11 al asistente desde la misma IP responde 429 con Retry-After y el mensaje")
    void laOnceResponde429() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(10, 1000);
        for (int i = 0; i < 10; i++) {
            assertEquals(200, estado(filtro, "/api/asistente/turno", "203.0.113.7"));
        }
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/asistente/turno");
        peticion.addHeader("CF-Connecting-IP", "203.0.113.7");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        // Act
        filtro.doFilter(peticion, respuesta, new MockFilterChain());
        // Assert
        assertEquals(429, respuesta.getStatus());
        assertEquals("60", respuesta.getHeader("Retry-After"));
        assertEquals("{\"error\":\"" + FiltroLimitePeticiones.MENSAJE + "\"}", respuesta.getContentAsString());
    }

    @Test
    @DisplayName("La cubeta se recarga con el tiempo: 6 segundos después de agotarla entra una más")
    void seRecargaConElTiempo() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(10, 1000);
        for (int i = 0; i < 10; i++) {
            estado(filtro, "/api/asistente/turno", "203.0.113.7");
        }
        // Act
        reloj.addAndGet(6 * UN_SEGUNDO);
        int primera = estado(filtro, "/api/asistente/turno", "203.0.113.7");
        int segunda = estado(filtro, "/api/asistente/turno", "203.0.113.7");
        // Assert
        assertEquals(200, primera);
        assertEquals(429, segunda);
    }

    @Test
    @DisplayName("Cada IP tiene su propia cubeta")
    void cadaIpTieneSuCubeta() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(1, 1000);
        estado(filtro, "/api/asistente/turno", "203.0.113.7");
        // Act
        int otraIp = estado(filtro, "/api/asistente/turno", "198.51.100.9");
        // Assert
        assertEquals(200, otraIp);
    }

    @Test
    @DisplayName("El tope global del asistente frena aunque cada petición llegue con una IP distinta")
    void elGlobalFrenaIpsFalsas() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(10, 3);
        for (int i = 0; i < 3; i++) {
            assertEquals(200, estado(filtro, "/api/asistente/turno", "10.0.0." + i));
        }
        // Act
        int cuarta = estado(filtro, "/api/asistente/turno", "10.0.0.99");
        // Assert
        assertEquals(429, cuarta);
    }

    @Test
    @DisplayName("Voz y reportes tienen su propio límite, separado del asistente")
    void gruposSeparados() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(1, 1000);
        estado(filtro, "/api/asistente/turno", "203.0.113.7");
        // Act
        int voz = estado(filtro, "/api/voz", "203.0.113.7");
        int reportes = estado(filtro, "/api/reportes", "203.0.113.7");
        // Assert
        assertEquals(200, voz);
        assertEquals(200, reportes);
    }

    @Test
    @DisplayName("Sin CF-Connecting-IP se usa la IP de la conexión")
    void sinCabeceraUsaLaConexion() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(1, 1000);
        estado(filtro, "/api/asistente/turno", null);
        // Act
        int segunda = estado(filtro, "/api/asistente/turno", null);
        // Assert
        assertEquals(429, segunda);
    }

    @Test
    @DisplayName("Las rutas fuera del límite, como la página o la salud, nunca responden 429")
    void rutasSinLimite() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(1, 1);
        // Act y Assert
        for (int i = 0; i < 5; i++) {
            assertEquals(200, estado(filtro, "/actuator/health", "203.0.113.7"));
        }
    }

    @Test
    @DisplayName("Las cubetas quietas por más de un minuto se borran cuando el mapa llega al tope")
    void elMapaNoCreceSinTope() throws Exception {
        // Arrange
        FiltroLimitePeticiones filtro = filtro(10, Integer.MAX_VALUE);
        for (int i = 0; i < FiltroLimitePeticiones.MAXIMO_CUBETAS; i++) {
            estado(filtro, "/api/voz", "ip-" + i);
        }
        reloj.addAndGet(61 * UN_SEGUNDO);
        // Act
        int nueva = estado(filtro, "/api/voz", "ip-nueva");
        // Assert
        assertEquals(200, nueva);
        assertEquals(1, filtro.cubetasEnMemoria());
    }

    @Test
    @DisplayName("Un límite de cero se rechaza al arrancar")
    void limiteCeroSeRechaza() {
        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> new Limites(0, 60, 3000, 3000));
    }
}
