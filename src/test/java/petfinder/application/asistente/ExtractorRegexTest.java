package petfinder.application.asistente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import petfinder.application.service.ExtractorRegex;
import petfinder.application.service.InterpreteComandoVoz;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.TipoReporte;

/**
 * El extractor es lógica pura: se prueba con el intérprete real y sin dobles.
 * Las clases de equivalencia son las formas de decir el mismo dato (celular
 * con espacios, con +57, con guiones) y los valores límite son los números
 * que casi lo son (9 y 11 dígitos, o que no empiezan por 3).
 */
class ExtractorRegexTest {

    private final ExtractorRegex extractor = new ExtractorRegex(new InterpreteComandoVoz());

    private Optional<BorradorReporte> extraer(String texto) {
        return extractor.extraer(texto, BorradorReporte.vacio());
    }

    @Test
    @DisplayName("Se identifica como el extractor 'regex'")
    void nombreDelExtractor() {
        // Arrange + Act + Assert
        assertEquals("regex", extractor.nombre());
    }

    @Test
    @DisplayName("Una pérdida completa con contacto extrae tipo, nombre, especie, zona y celular")
    void perdidaCompletaConContacto() {
        // Arrange
        String frase = "perdí un perro llamado Max en Chía, mi número es 300 123 4567";
        // Act
        BorradorReporte borrador = extraer(frase).orElseThrow();
        // Assert
        assertEquals(TipoReporte.PERDIDA, borrador.tipo());
        assertEquals("Max", borrador.nombre());
        assertEquals("perro", borrador.especie());
        assertEquals("Chía", borrador.zona());
        assertEquals("3001234567", borrador.contactoMedio());
    }

    @Test
    @DisplayName("La zona conserva las tildes aunque el intérprete de voz las quite")
    void zonaConservaTildes() {
        // Arrange + Act
        BorradorReporte borrador = extraer("perdí un perro llamado Max en Chía").orElseThrow();
        // Assert
        assertEquals("Chía", borrador.zona());
    }

    @Test
    @DisplayName("Sin coma antes del contacto la zona no arrastra 'mi número es'")
    void zonaSinComaAntesDelContacto() {
        // Arrange + Act
        BorradorReporte borrador = extraer("perdí un perro llamado Max en Chía mi número es 3001234567").orElseThrow();
        // Assert
        assertEquals("Chía", borrador.zona());
        assertEquals("3001234567", borrador.contactoMedio());
    }

    @ParameterizedTest(name = "[{0}] → {1}")
    @DisplayName("El celular se reconoce con espacios, guiones, puntos o +57 y se guarda sin separadores")
    @CsvSource(delimiter = '|', value = {
            "llámame al 300 123 4567     | 3001234567",
            "llámame al 3001234567       | 3001234567",
            "llámame al +57 300 123 4567 | 3001234567",
            "llámame al 300-123-4567     | 3001234567",
            "llámame al 300.123.4567     | 3001234567",
            "mi celular es 3109876543    | 3109876543"
    })
    void reconoceCelular(String frase, String esperado) {
        // Arrange + Act
        BorradorReporte borrador = extraer(frase).orElseThrow();
        // Assert
        assertEquals(esperado, borrador.contactoMedio());
    }

    @ParameterizedTest(name = "[{0}] no es un celular")
    @DisplayName("Los números que casi son un celular no se toman como contacto")
    @ValueSource(strings = {
            "el número es 300123456",
            "el número es 30012345678",
            "el número es 2001234567"
    })
    void noEsCelular(String frase) {
        // Arrange + Act
        Optional<BorradorReporte> resultado = extraer(frase);
        // Assert
        assertTrue(resultado.isEmpty() || resultado.get().contactoMedio() == null);
    }

    @Test
    @DisplayName("Un correo dicho en cualquier parte de la frase se toma como contacto")
    void reconoceCorreo() {
        // Arrange + Act
        BorradorReporte borrador = extraer("escríbeme a ana.lopez@correo.com por favor").orElseThrow();
        // Assert
        assertEquals("ana.lopez@correo.com", borrador.contactoMedio());
    }

    @Test
    @DisplayName("Un hallazgo extrae tipo, descripción del animal, zona y especie")
    void hallazgo() {
        // Arrange + Act
        BorradorReporte borrador = extraer("encontré una gata blanca en Cajicá").orElseThrow();
        // Assert
        assertEquals(TipoReporte.ENCONTRADA, borrador.tipo());
        assertEquals("gata blanca", borrador.descripcionMascota());
        assertEquals("Cajicá", borrador.zona());
        assertEquals("gato", borrador.especie());
    }

    @Test
    @DisplayName("El nombre se toma de la palabra con mayúscula que sigue a la especie")
    void nombreTrasLaEspecie() {
        // Arrange + Act
        BorradorReporte borrador = extraer("se me perdió mi perrita Luna en Cedritos").orElseThrow();
        // Assert
        assertEquals(TipoReporte.PERDIDA, borrador.tipo());
        assertEquals("Luna", borrador.nombre());
        assertEquals("perro", borrador.especie());
        assertEquals("Cedritos", borrador.zona());
    }

    @Test
    @DisplayName("Un nombre dicho en minúscula se guarda con mayúscula inicial")
    void nombreEnMinuscula() {
        // Arrange + Act
        BorradorReporte borrador = extraer("mi perrito llamado max se perdió").orElseThrow();
        // Assert
        assertEquals("Max", borrador.nombre());
    }

    @Test
    @DisplayName("Sin nombre en la frase, el nombre queda vacío")
    void sinNombre() {
        // Arrange + Act
        BorradorReporte borrador = extraer("perdí mi perro").orElseThrow();
        // Assert
        assertNull(borrador.nombre());
    }

    @Test
    @DisplayName("Un hallazgo que menciona 'perdido' sigue siendo un hallazgo: gana la primera raíz")
    void gananLasPrimerasRaices() {
        // Arrange + Act
        BorradorReporte borrador = extraer("encontré un perro perdido en Usaquén").orElseThrow();
        // Assert
        assertEquals(TipoReporte.ENCONTRADA, borrador.tipo());
    }

    @Test
    @DisplayName("Si la frase nombra dos animales, la especie es la del primero")
    void especieDelPrimerAnimal() {
        // Arrange + Act
        BorradorReporte borrador = extraer("encontré un gato que perseguía a un perro en Suba").orElseThrow();
        // Assert
        assertEquals("gato", borrador.especie());
    }

    @Test
    @DisplayName("Con varios 'en' la zona es el último lugar mencionado")
    void zonaEsElUltimoLugar() {
        // Arrange + Act
        BorradorReporte borrador = extraer("tengo en mi casa una gatita gris que apareció en el portal").orElseThrow();
        // Assert
        assertEquals("El portal", borrador.zona());
        assertEquals("gato", borrador.especie());
    }

    @ParameterizedTest(name = "[{0}] no aporta datos")
    @DisplayName("Una frase sin datos o un texto vacío no produce borrador")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "hola", "gracias"})
    void sinDatos(String frase) {
        // Arrange + Act
        Optional<BorradorReporte> resultado = extraer(frase);
        // Assert
        assertTrue(resultado.isEmpty());
    }
}
