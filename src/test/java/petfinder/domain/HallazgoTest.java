package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.TipoAvistamiento;
import petfinder.domain.model.Ubicacion;

/**
 * "La tengo yo": un hallazgo es un avistamiento de otro tipo que exige
 * contacto y marca que alguien tiene a la mascota, sin cerrar el caso.
 */
class HallazgoTest {

    private static final Ubicacion CASA = new Ubicacion("Palatino", "Portería del conjunto");

    private static ReportePerdida perdidaDeCopito() {
        return new ReportePerdida("PF-900", new Ubicacion("Usaquén", "Parque"), "Se escapó",
                new Mascota("Copito", "perro", "criollo", "blanco", null), new Contacto("Camila", "3001234567"));
    }

    private static Avistamiento hallazgo(Contacto contacto) {
        return new Avistamiento("AV-1", LocalDateTime.now(), CASA, "Está en mi casa", contacto,
                TipoAvistamiento.LA_TENGO);
    }

    @Test
    @DisplayName("Decir que la tienes sin dejar contacto se rechaza con un mensaje para la persona")
    void hallazgoSinContactoSeRechaza() {
        // Arrange
        Contacto soloNombre = new Contacto("Andrés", " ");

        // Act
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class, () -> hallazgo(soloNombre));
        assertThrows(DatosInvalidosException.class, () -> hallazgo(null));

        // Assert
        assertEquals("Si la tienes contigo, deja tu contacto para que su familia te encuentre.", error.getMessage());
    }

    @Test
    @DisplayName("Un avistamiento sin tipo es una pista (La vi) y no exige contacto")
    void sinTipoEsPista() {
        // Arrange & Act
        Avistamiento pista = new Avistamiento("AV-2", LocalDateTime.now(), CASA, "La vi pasar", null, null);
        Avistamiento comoAntes = new Avistamiento("AV-3", LocalDateTime.now(), CASA, "La vi pasar", null);

        // Assert
        assertEquals(TipoAvistamiento.LA_VI, pista.tipo());
        assertEquals(TipoAvistamiento.LA_VI, comoAntes.tipo());
        assertFalse(pista.esHallazgo());
    }

    @Test
    @DisplayName("El caso sabe que alguien tiene a la mascota solo cuando llega un hallazgo, y sigue activo")
    void laTieneAlguienSoloConHallazgo() {
        // Arrange
        ReportePerdida copito = perdidaDeCopito();
        copito.agregarAvistamiento(new Avistamiento("AV-4", LocalDateTime.now(), CASA, "La vi pasar", null));
        boolean conSoloPista = copito.laTieneAlguien();

        // Act
        copito.agregarAvistamiento(hallazgo(new Contacto("Andrés", "3109876543")));

        // Assert
        assertFalse(conSoloPista);
        assertTrue(copito.laTieneAlguien());
        assertTrue(copito.estaActivo());
    }
}
