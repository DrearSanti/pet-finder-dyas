package petfinder.adaptadores.entrada.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Qué rutas reciben las cabeceras; las cabeceras por HTTP real están en CabecerasSeguridadSistemaIT. */
class FiltroCabecerasSeguridadTest {

    private final FiltroCabecerasSeguridad filtro = new FiltroCabecerasSeguridad();

    @Test
    @DisplayName("Una ruta normal recibe la política y sigue la cadena")
    void rutaNormalRecibeCabeceras() throws Exception {
        // Arrange
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        // Act
        filtro.doFilter(peticion, respuesta, cadena);
        // Assert
        assertEquals(FiltroCabecerasSeguridad.POLITICA_CONTENIDO, respuesta.getHeader("Content-Security-Policy"));
        assertNotNull(cadena.getRequest());
    }

    @Test
    @DisplayName("La consola de H2 del perfil local queda sin la política, para que pueda abrir")
    void consolaH2SinPolitica() throws Exception {
        // Arrange
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/h2-console/");
        peticion.setRequestURI("/h2-console/");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        // Act
        filtro.doFilter(peticion, respuesta, new MockFilterChain());
        // Assert
        assertNull(respuesta.getHeader("Content-Security-Policy"));
    }
}
