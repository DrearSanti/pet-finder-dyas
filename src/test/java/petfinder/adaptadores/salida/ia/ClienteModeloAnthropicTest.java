package petfinder.adaptadores.salida.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Revisa la forma del esquema de salida sin llamar a la API. El resto de las
 * pruebas usa un doble del cliente, así que un esquema que la API rechaza
 * pasaba en verde y solo fallaba con una clave real (400 en Render). Estas
 * pruebas atrapan ese error antes de desplegar.
 */
class ClienteModeloAnthropicTest {

    @Test
    @DisplayName("Ningún enum del esquema convive con una lista de tipos, porque la API lo rechaza")
    void ningunEnumConListaDeTipos() {
        // Arrange
        Map<String, Object> esquema = ClienteModeloAnthropic.definicionDelEsquema();

        // Act
        List<Map<?, ?>> invalidos = new ArrayList<>();
        buscarEnumConListaDeTipos(esquema, invalidos);

        // Assert
        assertTrue(invalidos.isEmpty(), "Definiciones con enum y \"type\" como lista: " + invalidos);
    }

    @Test
    @DisplayName("El tipo solo admite PERDIDA, ENCONTRADA o null")
    void tipoLimitadoAPerdidaOEncontrada() {
        // Arrange
        Map<?, ?> propiedades = (Map<?, ?>) ClienteModeloAnthropic.definicionDelEsquema().get("properties");

        // Act
        Map<?, ?> tipo = (Map<?, ?>) propiedades.get("tipo");

        // Assert
        assertEquals(Map.of("anyOf", List.of(
                Map.of("type", "string", "enum", List.of("PERDIDA", "ENCONTRADA")),
                Map.of("type", "null"))), tipo);
    }

    @Test
    @DisplayName("Los doce campos son obligatorios y el modelo no puede inventar otros")
    void camposCerrados() {
        // Arrange
        Map<String, Object> esquema = ClienteModeloAnthropic.definicionDelEsquema();

        // Act
        List<?> requeridos = (List<?>) esquema.get("required");
        Map<?, ?> propiedades = (Map<?, ?>) esquema.get("properties");

        // Assert
        assertEquals(12, requeridos.size());
        assertEquals(List.copyOf(propiedades.keySet()), requeridos);
        assertEquals(false, esquema.get("additionalProperties"));
    }

    private static void buscarEnumConListaDeTipos(Object nodo, List<Map<?, ?>> invalidos) {
        if (nodo instanceof Map<?, ?> mapa) {
            if (mapa.containsKey("enum") && mapa.get("type") instanceof List) {
                invalidos.add(mapa);
            }
            mapa.values().forEach(valor -> buscarEnumConListaDeTipos(valor, invalidos));
        } else if (nodo instanceof List<?> lista) {
            lista.forEach(valor -> buscarEnumConListaDeTipos(valor, invalidos));
        }
    }
}
