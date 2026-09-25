package petfinder.application.voz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;
import petfinder.application.service.InterpreteComandoVoz;
import petfinder.application.service.InterpreteComandoVoz.Comando;

/**
 * Clases de equivalencia de las frases (cada forma de decir un comando) y
 * valores límite del texto (nulo, vacío, 200 y 201 caracteres).
 */
class InterpreteComandoVozTest {

    @ParameterizedTest(name = "[{0}] → {1}, {2}, {3}")
    @DisplayName("Una pérdida dictada se reconoce con especie, nombre y zona, sin importar tildes ni mayúsculas")
    @CsvSource(delimiter = '|', value = {
            "perdí un perro llamado Max en Chía        | perro | Max  | Chia",
            "Perdi una gata llamada Nala en Cedritos   | gato  | Nala | Cedritos",
            "PERDÍ UN PERRO LLAMADO MAX EN CHÍA        | perro | Max  | Chia",
            "Se me perdió mi perrita que se llama Luna en La Calera. | perro | Luna | La Calera"
    })
    void reconocePerdida(String frase, String especie, String nombre, String zona) {
        // Arrange
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(AccionVoz.REGISTRAR_PERDIDA, comando.accion());
        assertEquals(especie, comando.especie());
        assertEquals(nombre, comando.nombre());
        assertEquals(zona, comando.zona());
    }

    @Test
    @DisplayName("Un hallazgo dictado se reconoce con la descripción del animal y la zona")
    void reconoceEncontrada() {
        // Arrange
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar("encontré una gata blanca en Cajicá");
        // Assert
        assertEquals(AccionVoz.REGISTRAR_ENCONTRADA, comando.accion());
        assertEquals("gata blanca", comando.descripcion());
        assertEquals("Cajica", comando.zona());
    }

    @ParameterizedTest(name = "[{0}]")
    @DisplayName("Ver, listar y mostrar reportes piden los casos activos")
    @ValueSource(strings = {"ver reportes", "listar reportes", "mostrar reportes", "¿Ver los reportes activos?"})
    void reconoceListado(String frase) {
        // Arrange
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(AccionVoz.LISTAR_ACTIVOS, comando.accion());
    }

    @ParameterizedTest(name = "[{0}] → PF-001")
    @DisplayName("Una consulta se reconoce y el número se rellena a tres dígitos")
    @ValueSource(strings = {"consultar reporte PF-001", "consultar reporte pf 1", "consultar reporte pf001"})
    void reconoceConsulta(String frase) {
        // Arrange
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(AccionVoz.CONSULTAR, comando.accion());
        assertEquals("PF-001", comando.idReporte());
    }

    @ParameterizedTest(name = "[{0}]")
    @DisplayName("Una frase vacía, incompleta o ajena a Pet Finder no se reconoce")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "cómprame un perro",
            "perdí un perro llamado Max",
            "perdí un llamado Max en Chía",
            "encontré en Cajicá"
    })
    void noReconoce(String frase) {
        // Arrange
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(AccionVoz.NO_RECONOCIDO, comando.accion());
    }

    @Test
    @DisplayName("Una frase válida de exactamente 200 caracteres todavía se reconoce")
    void doscientosCaracteresSeReconoce() {
        // Arrange
        String frase = fraseDeLongitud(InterpreteComandoVoz.LONGITUD_MAXIMA);
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(200, frase.length());
        assertEquals(AccionVoz.REGISTRAR_PERDIDA, comando.accion());
    }

    @Test
    @DisplayName("La misma frase con 201 caracteres se rechaza")
    void doscientosUnCaracteresSeRechaza() {
        // Arrange
        String frase = fraseDeLongitud(InterpreteComandoVoz.LONGITUD_MAXIMA + 1);
        InterpreteComandoVoz interprete = new InterpreteComandoVoz();
        // Act
        Comando comando = interprete.interpretar(frase);
        // Assert
        assertEquals(201, frase.length());
        assertEquals(AccionVoz.NO_RECONOCIDO, comando.accion());
    }

    /** Una pérdida válida cuya zona se alarga hasta llegar a la longitud pedida. */
    private static String fraseDeLongitud(int longitud) {
        String inicio = "perdí un perro llamado Max en C";
        return inicio + "a".repeat(longitud - inicio.length());
    }
}
