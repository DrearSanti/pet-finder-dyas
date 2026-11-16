package petfinder.adaptadores.entrada.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * La clave de CARTO llega por variable de entorno y nunca rompe el script:
 * sin clave, 200 y cuerpo vacío; con una clave rara, solo sus caracteres válidos.
 */
@WebMvcTest(controllers = ConfiguracionMapaController.class, properties = "PETFINDER_CLAVE_CARTO=")
class ConfiguracionMapaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Sin PETFINDER_CLAVE_CARTO, /js/config-local.js responde 200 con cuerpo vacío")
    void sinClaveRespondeVacio() throws Exception {
        // Act & Assert
        mvc.perform(get("/js/config-local.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"))
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Con clave, el script define window.PETFINDER_CLAVE_CARTO con ese valor")
    void conClaveDefineLaVariable() {
        // Arrange
        ConfiguracionMapaController controlador = new ConfiguracionMapaController("clave_de-prueba123");

        // Act
        String cuerpo = controlador.configuracion().getBody();

        // Assert
        assertEquals("window.PETFINDER_CLAVE_CARTO = 'clave_de-prueba123';\n", cuerpo);
    }

    @Test
    @DisplayName("Una clave con comillas o paréntesis no puede cerrar la cadena del script")
    void claveConCaracteresRarosSeSanea() {
        // Arrange
        ConfiguracionMapaController controlador = new ConfiguracionMapaController("abc'); alert(1); //");

        // Act
        String cuerpo = controlador.configuracion().getBody();

        // Assert
        assertEquals("window.PETFINDER_CLAVE_CARTO = 'abcalert1';\n", cuerpo);
    }
}
