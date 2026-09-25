package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import petfinder.domain.model.EstadoReporte;

/** Matriz completa de transiciones: 3 estados de origen × 3 de destino. */
class EstadoReporteTest {

    @ParameterizedTest(name = "{0} → {1} permitido = {2}")
    @DisplayName("Solo un reporte ACTIVO puede pasar a RESUELTO o CERRADO")
    @CsvSource({
            "ACTIVO,   ACTIVO,   false",
            "ACTIVO,   RESUELTO, true",
            "ACTIVO,   CERRADO,  true",
            "RESUELTO, ACTIVO,   false",
            "RESUELTO, RESUELTO, false",
            "RESUELTO, CERRADO,  false",
            "CERRADO,  ACTIVO,   false",
            "CERRADO,  RESUELTO, false",
            "CERRADO,  CERRADO,  false"
    })
    void matrizDeTransiciones(EstadoReporte origen, EstadoReporte destino, boolean permitido) {
        // Arrange: los valores llegan de la tabla
        // Act
        boolean resultado = origen.permiteTransicionA(destino);
        // Assert
        assertEquals(permitido, resultado);
    }
}
