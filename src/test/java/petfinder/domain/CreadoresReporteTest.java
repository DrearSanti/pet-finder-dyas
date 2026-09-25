package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Clases de equivalencia de los datos obligatorios: nulo, vacío y solo
 * espacios son inválidos para cada campo que exigen los creadores.
 */
class CreadoresReporteTest {

    private final CreadorReportePerdida creadorPerdida = new CreadorReportePerdida(new GeneradorIdReportes());
    private final CreadorReporteEncontrada creadorEncontrada = new CreadorReporteEncontrada(new GeneradorIdReportes());

    static Stream<Arguments> perdidasInvalidas() {
        Mascota luna = new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra");
        Contacto camila = new Contacto("Camila", "3001234567");
        Ubicacion cedritos = new Ubicacion("Cedritos", "Parque 147");
        return Stream.of(
                Arguments.of("sin zona", SolicitudReporte.paraPerdida(new Ubicacion(" ", "x"), "Se perdió", camila, luna)),
                Arguments.of("sin descripción", SolicitudReporte.paraPerdida(cedritos, "", camila, luna)),
                Arguments.of("sin medio de contacto", SolicitudReporte.paraPerdida(cedritos, "Se perdió", new Contacto("Camila", null), luna)),
                Arguments.of("sin mascota", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, null)),
                Arguments.of("mascota sin nombre", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, new Mascota("  ", "perro", "", "", ""))),
                Arguments.of("mascota sin especie", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, new Mascota("Luna", null, "", "", ""))));
    }

    @ParameterizedTest(name = "pérdida {0}")
    @MethodSource("perdidasInvalidas")
    @DisplayName("Una pérdida con un dato obligatorio faltante se rechaza")
    void perdidaInvalidaSeRechaza(String caso, SolicitudReporte solicitud) {
        // Act + Assert
        assertThrows(DatosInvalidosException.class, () -> creadorPerdida.preparar(solicitud));
    }

    @ParameterizedTest(name = "descripción del animal = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Un hallazgo sin descripción del animal se rechaza")
    void encontradaSinDescripcionSeRechaza(String descripcionAnimal) {
        // Arrange
        SolicitudReporte solicitud = SolicitudReporte.paraEncontrada(
                new Ubicacion("Chía", "Parque principal"), "Lo encontré solo",
                new Contacto("Lorenzi", "3007654321"), descripcionAnimal);
        // Act + Assert
        assertThrows(DatosInvalidosException.class, () -> creadorEncontrada.preparar(solicitud));
    }
}
