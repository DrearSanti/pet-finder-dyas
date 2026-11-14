package petfinder.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.model.CodigoGestion;

/** Cómo config convierte PETFINDER_CODIGO_ADMIN en la huella que recibe el servicio. */
class CodigoAdministradorTest {

    @Test
    @DisplayName("Sin código de administrador no hay huella: nadie es administrador")
    void vacioEsSinAdministrador() {
        // Act y Assert
        assertNull(ConfiguracionPetFinder.huellaAdministrador(null));
        assertNull(ConfiguracionPetFinder.huellaAdministrador(""));
        assertNull(ConfiguracionPetFinder.huellaAdministrador("   "));
    }

    @Test
    @DisplayName("Un código de administrador de menos de 20 caracteres no deja arrancar la app")
    void cortoNoArranca() {
        // Act
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ConfiguracionPetFinder.huellaAdministrador("corto-19-caracteres"));
        // Assert
        assertFalse(error.getMessage().contains("corto-19-caracteres"), "El mensaje no debe repetir el código");
    }

    @Test
    @DisplayName("Un código válido se convierte en su huella y no se guarda en claro")
    void validoSeConvierteEnHuella() {
        // Arrange
        String codigo = "un-codigo-de-administrador-largo";
        // Act
        String huella = ConfiguracionPetFinder.huellaAdministrador(codigo);
        // Assert
        assertEquals(CodigoGestion.huellaDe(codigo), huella);
    }
}
