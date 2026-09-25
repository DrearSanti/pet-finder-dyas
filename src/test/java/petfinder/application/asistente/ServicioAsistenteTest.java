package petfinder.application.asistente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import petfinder.application.port.entrada.AsistenteReportes.ResultadoTurno;
import petfinder.application.port.salida.ExtractorDatosReporte;
import petfinder.application.service.ServicioAsistente;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.BorradorReporte.Campo;
import petfinder.domain.model.TipoReporte;

/**
 * El servicio se prueba contra dobles de los extractores: lo que importa es
 * el orden en que los prueba, qué hace cuando fallan y qué pregunta después,
 * no cómo entiende cada uno una frase (eso lo cubre ExtractorRegexTest).
 */
@ExtendWith(MockitoExtension.class)
class ServicioAsistenteTest {

    private static final String FRASE = "perdí un perro llamado Max en Chía";

    @Mock
    private ExtractorDatosReporte primero;

    @Mock
    private ExtractorDatosReporte segundo;

    private ServicioAsistente servicio() {
        return new ServicioAsistente(List.of(primero, segundo));
    }

    private static BorradorReporte borrador(TipoReporte tipo, String nombre, String especie, String zona,
                                            String descripcion, String contactoMedio) {
        return new BorradorReporte(tipo, nombre, especie, null, null, null, null,
                zona, null, null, null, descripcion, null, contactoMedio);
    }

    @Test
    @DisplayName("Si el primer extractor responde, fusiona su resultado y lo reporta como fuente")
    void primerExtractorResponde() {
        // Arrange
        when(primero.extraer(eq(FRASE), any())).thenReturn(
                Optional.of(borrador(TipoReporte.PERDIDA, "Max", "perro", "Chía", null, null)));
        when(primero.nombre()).thenReturn("claude");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), FRASE);
        // Assert
        assertEquals("claude", resultado.fuente());
        assertEquals("Max", resultado.borrador().nombre());
        assertEquals("Chía", resultado.borrador().zona());
        verifyNoInteractions(segundo);
    }

    @Test
    @DisplayName("Si el primer extractor lanza una excepción, el turno sigue con el segundo")
    void primerExtractorFalla() {
        // Arrange
        when(primero.extraer(any(), any())).thenThrow(new IllegalStateException("sin red"));
        when(segundo.extraer(eq(FRASE), any())).thenReturn(
                Optional.of(borrador(TipoReporte.PERDIDA, "Max", null, null, null, null)));
        when(segundo.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), FRASE);
        // Assert
        assertEquals("regex", resultado.fuente());
        assertEquals("Max", resultado.borrador().nombre());
    }

    @Test
    @DisplayName("Si el primer extractor devuelve vacío, el turno sigue con el segundo")
    void primerExtractorVacio() {
        // Arrange
        when(primero.extraer(any(), any())).thenReturn(Optional.empty());
        when(segundo.extraer(eq(FRASE), any())).thenReturn(
                Optional.of(borrador(null, null, null, "Chía", null, null)));
        when(segundo.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), FRASE);
        // Assert
        assertEquals("regex", resultado.fuente());
        assertEquals("Chía", resultado.borrador().zona());
    }

    @Test
    @DisplayName("Si ningún extractor responde, el borrador queda igual y la fuente es 'ninguna'")
    void ningunoResponde() {
        // Arrange
        BorradorReporte actual = borrador(TipoReporte.PERDIDA, "Luna", "perro", null, null, null);
        when(primero.extraer(any(), any())).thenThrow(new IllegalStateException("sin red"));
        when(segundo.extraer(any(), any())).thenReturn(Optional.empty());
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(actual, "mmm");
        // Assert
        assertEquals(actual, resultado.borrador());
        assertEquals("ninguna", resultado.fuente());
        assertEquals(Campo.ZONA.pregunta(), resultado.pregunta());
    }

    @Test
    @DisplayName("Lo que la persona ya había dicho se conserva cuando el turno trae datos nuevos")
    void conservaLoYaDicho() {
        // Arrange
        BorradorReporte actual = borrador(TipoReporte.PERDIDA, "Luna", "perro", "Cedritos", "Se perdió", null);
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(null, null, null, null, null, "3001234567")));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(actual, "3001234567");
        // Assert
        assertEquals("Luna", resultado.borrador().nombre());
        assertEquals("Cedritos", resultado.borrador().zona());
        assertEquals("3001234567", resultado.borrador().contactoMedio());
    }

    @Test
    @DisplayName("Con datos faltantes, la pregunta es la del primer campo que falta")
    void preguntaDelPrimerFaltante() {
        // Arrange
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(TipoReporte.PERDIDA, null, null, "Chía", null, null)));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), "perdí algo en Chía");
        // Assert
        assertEquals(List.of(Campo.NOMBRE, Campo.ESPECIE, Campo.CONTACTO), resultado.faltantes());
        assertEquals(Campo.NOMBRE.pregunta(), resultado.pregunta());
        assertFalse(resultado.listoParaPublicar());
    }

    @Test
    @DisplayName("Con la tarjeta completa, avisa que ya se puede revisar y publicar")
    void tarjetaCompleta() {
        // Arrange
        BorradorReporte actual = borrador(TipoReporte.PERDIDA, "Luna", "perro", "Cedritos", "Se perdió hoy", null);
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(null, null, null, null, null, "3001234567")));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(actual, "3001234567");
        // Assert
        assertTrue(resultado.faltantes().isEmpty());
        assertEquals("Listo. Revisa la tarjeta y publica.", resultado.pregunta());
        assertTrue(resultado.listoParaPublicar());
    }

    @Test
    @DisplayName("Si el turno trae el tipo y la tarjeta no tiene descripción, la frase es la descripción")
    void fraseComoDescripcion() {
        // Arrange
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(TipoReporte.PERDIDA, "Max", "perro", "Chía", null, null)));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), "  " + FRASE + "  ");
        // Assert
        assertEquals(FRASE, resultado.borrador().descripcion());
    }

    @Test
    @DisplayName("Si la tarjeta ya tiene descripción, un turno nuevo no la reemplaza")
    void noPisaLaDescripcion() {
        // Arrange
        BorradorReporte actual = borrador(TipoReporte.PERDIDA, null, null, null, "Se escapó anoche", null);
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(TipoReporte.PERDIDA, "Max", "perro", "Chía", null, null)));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(actual, FRASE);
        // Assert
        assertEquals("Se escapó anoche", resultado.borrador().descripcion());
    }

    @Test
    @DisplayName("Si el turno no trae tipo, la frase no se toma como descripción")
    void sinTipoNoHayDescripcion() {
        // Arrange
        when(primero.extraer(any(), any())).thenReturn(
                Optional.of(borrador(null, null, null, null, null, "3001234567")));
        when(primero.nombre()).thenReturn("regex");
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(BorradorReporte.vacio(), "3001234567");
        // Assert
        assertNull(resultado.borrador().descripcion());
    }

    @ParameterizedTest(name = "[{0}] deja el borrador tal cual")
    @DisplayName("Un texto nulo, vacío o en blanco no consulta extractores y repite la pregunta pendiente")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void textoVacio(String texto) {
        // Arrange
        BorradorReporte actual = borrador(TipoReporte.PERDIDA, "Luna", null, null, null, null);
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(actual, texto);
        // Assert
        assertEquals(actual, resultado.borrador());
        assertEquals("ninguna", resultado.fuente());
        assertEquals(Campo.ESPECIE.pregunta(), resultado.pregunta());
        verifyNoInteractions(primero, segundo);
    }

    @Test
    @DisplayName("Sin borrador previo, el primer turno parte de una tarjeta vacía")
    void sinBorradorPrevio() {
        // Arrange
        when(primero.extraer(eq("hola"), eq(BorradorReporte.vacio()))).thenReturn(Optional.empty());
        when(segundo.extraer(eq("hola"), eq(BorradorReporte.vacio()))).thenReturn(Optional.empty());
        // Act
        ResultadoTurno resultado = servicio().procesarTurno(null, "hola");
        // Assert
        assertEquals(BorradorReporte.vacio(), resultado.borrador());
        assertEquals(Campo.TIPO.pregunta(), resultado.pregunta());
    }
}
