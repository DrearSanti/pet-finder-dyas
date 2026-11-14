package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.model.CodigoGestion;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.Ubicacion;

class CodigoGestionTest {

    @Test
    @DisplayName("Un código nuevo tiene 22 caracteres de Base64URL y no se repite")
    void generarEsAleatorioYCopiable() {
        // Arrange
        Set<String> vistos = new HashSet<>();
        // Act
        for (int i = 0; i < 1000; i++) {
            vistos.add(CodigoGestion.generar().valor());
        }
        // Assert
        assertEquals(1000, vistos.size());
        vistos.forEach(c -> assertTrue(c.matches("[A-Za-z0-9_-]{22}"), c));
    }

    @Test
    @DisplayName("La huella es SHA-256 en hexadecimal: 64 caracteres y distinta del código")
    void huellaEsSha256() {
        // Arrange
        CodigoGestion codigo = CodigoGestion.generar();
        // Act
        String huella = codigo.huella();
        // Assert
        assertTrue(huella.matches("[0-9a-f]{64}"));
        assertNotEquals(codigo.valor(), huella);
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", CodigoGestion.huellaDe("abc"));
    }

    @Test
    @DisplayName("Coincide solo con el código correcto; vacío, nulo o sin huella nunca coinciden")
    void coincide() {
        // Arrange
        CodigoGestion codigo = CodigoGestion.generar();
        String huella = codigo.huella();
        // Act y Assert
        assertTrue(CodigoGestion.coincide(codigo.valor(), huella));
        assertFalse(CodigoGestion.coincide(CodigoGestion.generar().valor(), huella));
        assertFalse(CodigoGestion.coincide(null, huella));
        assertFalse(CodigoGestion.coincide("", huella));
        assertFalse(CodigoGestion.coincide(codigo.valor(), null));
        assertFalse(CodigoGestion.coincide("", CodigoGestion.huellaDe("")), "Un código vacío nunca abre nada");
    }

    @Test
    @DisplayName("toString no muestra el código, para que no termine en un log")
    void toStringLoOculta() {
        // Arrange
        CodigoGestion codigo = CodigoGestion.generar();
        // Act y Assert
        assertFalse(codigo.toString().contains(codigo.valor()));
    }

    @Test
    @DisplayName("Un caso recibe la huella una sola vez: la segunda se rechaza")
    void laHuellaSeFijaUnaVez() {
        // Arrange
        ReporteEncontrada reporte = new ReporteEncontrada("PF-900", new Ubicacion("Suba", "Portal"),
                "Estaba sola", "Perro café", new Contacto("Ana", "3001112233"));
        CodigoGestion codigo = CodigoGestion.generar();
        reporte.protegerConHuella(codigo.huella());
        // Act y Assert
        assertTrue(reporte.admiteCodigo(codigo.valor()));
        assertThrows(OperacionNoPermitidaException.class,
                () -> reporte.protegerConHuella(CodigoGestion.generar().huella()));
    }

    @Test
    @DisplayName("Un caso sin huella (publicado antes de S9) no admite ningún código")
    void sinHuellaNoAdmiteNada() {
        // Arrange
        ReporteEncontrada viejo = new ReporteEncontrada("PF-901", new Ubicacion("Suba", "Portal"),
                "Estaba sola", "Perro café", new Contacto("Ana", "3001112233"));
        // Act y Assert
        assertFalse(viejo.admiteCodigo(CodigoGestion.generar().valor()));
    }
}
