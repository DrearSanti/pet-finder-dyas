package petfinder.adaptadores.entrada.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * El filtro se prueba solo, sin Spring: lo que importa es qué hace con el
 * tamaño declarado y con un cuerpo que llega por partes sin declararlo. La
 * prueba de 413 con el servidor real está en LimitesEntradaSistemaIT.
 */
class FiltroTamanoCuerpoTest {

    private final FiltroTamanoCuerpo filtro = new FiltroTamanoCuerpo();

    /** Petición que llega por partes (chunked): el servidor no sabe su tamaño de antemano. */
    private static MockHttpServletRequest porPartes(byte[] cuerpo) {
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/asistente/turno") {
            @Override
            public int getContentLength() {
                return -1;
            }

            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        peticion.setContent(cuerpo);
        return peticion;
    }

    @Test
    @DisplayName("Un cuerpo declarado de más de 64 KB responde 413 y no sigue la cadena")
    void declaradoGrandeResponde413() throws Exception {
        // Arrange
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/voz");
        peticion.setContent(new byte[FiltroTamanoCuerpo.TAMANO_MAXIMO + 1]);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        // Act
        filtro.doFilter(peticion, respuesta, cadena);
        // Assert
        assertEquals(413, respuesta.getStatus());
        assertEquals("{\"error\":\"La petición es demasiado grande\"}",
                respuesta.getContentAsString(StandardCharsets.UTF_8));
        assertNull(cadena.getRequest());
    }

    @Test
    @DisplayName("Un cuerpo declarado de exactamente 64 KB sigue la cadena sin tocarse")
    void declaradoEnElLimiteSigue() throws Exception {
        // Arrange
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/voz");
        peticion.setContent(new byte[FiltroTamanoCuerpo.TAMANO_MAXIMO]);
        MockFilterChain cadena = new MockFilterChain();
        // Act
        filtro.doFilter(peticion, new MockHttpServletResponse(), cadena);
        // Assert
        assertSame(peticion, cadena.getRequest());
    }

    @Test
    @DisplayName("Un cuerpo por partes de más de 64 KB responde 413 aunque no declare su tamaño")
    void porPartesGrandeResponde413() throws Exception {
        // Arrange
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        // Act
        filtro.doFilter(porPartes(new byte[FiltroTamanoCuerpo.TAMANO_MAXIMO + 1]), respuesta, cadena);
        // Assert
        assertEquals(413, respuesta.getStatus());
        assertNull(cadena.getRequest());
    }

    @Test
    @DisplayName("Un cuerpo por partes pequeño llega al controlador completo y con su tamaño")
    void porPartesPequenoLlegaCompleto() throws Exception {
        // Arrange
        byte[] cuerpo = "{\"texto\":\"perdí un perro llamado Max en Chía\"}".getBytes(StandardCharsets.UTF_8);
        MockFilterChain cadena = new MockFilterChain();
        // Act
        filtro.doFilter(porPartes(cuerpo), new MockHttpServletResponse(), cadena);
        // Assert
        assertEquals(cuerpo.length, cadena.getRequest().getContentLength());
        assertArrayEquals(cuerpo, cadena.getRequest().getInputStream().readAllBytes());
    }
}
