package petfinder.domain.borrador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.Ubicacion;

class UbicacionTest {

    @ParameterizedTest(name = "lat {0}, lon {1} es válida")
    @DisplayName("Los extremos del rango de coordenadas son válidos")
    @CsvSource({"-90, -180", "90, 180", "0, 0", "4.7235, -74.0417"})
    void limitesValidos(double latitud, double longitud) {
        assertDoesNotThrow(() -> new Ubicacion("Cedritos", null, latitud, longitud));
    }

    @ParameterizedTest(name = "lat {0}, lon {1} se rechaza")
    @DisplayName("Un paso fuera del rango se rechaza")
    @CsvSource({"-90.0001, 0", "90.0001, 0", "0, -180.0001", "0, 180.0001"})
    void fueraDeRango(double latitud, double longitud) {
        assertThrows(DatosInvalidosException.class, () -> new Ubicacion("Cedritos", null, latitud, longitud));
    }

    @Test
    @DisplayName("Latitud sin longitud se rechaza")
    void coordenadaIncompleta() {
        assertThrows(DatosInvalidosException.class, () -> new Ubicacion("Cedritos", null, 4.7, null));
    }

    @Test
    @DisplayName("La versión pública redondea a tres decimales")
    void aproximadaRedondea() {
        // Arrange
        Ubicacion exacta = new Ubicacion("Cedritos", "Calle 140", 4.723581, -74.041749);
        // Act
        Ubicacion publica = exacta.aproximada();
        // Assert
        assertEquals(4.724, publica.latitud());
        assertEquals(-74.042, publica.longitud());
    }
}
