package petfinder.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Carga casos de ejemplo al arrancar, porque H2 en memoria empieza vacío en
 * cada reinicio (también en Render). Entra por el puerto GestionReportes, como
 * cualquier otro adaptador: los datos pasan por las mismas validaciones.
 * Solo corre en la aplicación web (la demo de consola conserva sus PF-001 y
 * PF-002 del Corte 1) y se apaga con petfinder.datos-ejemplo=false.
 */
@Component
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "petfinder.datos-ejemplo", havingValue = "true", matchIfMissing = true)
public class DatosDeEjemplo implements ApplicationRunner {

    private final GestionReportes gestion;

    public DatosDeEjemplo(GestionReportes gestion) {
        this.gestion = gestion;
    }

    @Override
    public void run(ApplicationArguments args) {
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Cedritos", "Parque de la 147", 4.7235, -74.0417),
                "Se escapó del conjunto esta tarde. Es tímida: si la ves, no la persigas.",
                new Contacto("Camila", "3001234567"),
                new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra en el lomo, collar rojo")));
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Usaquén", "Parque de Usaquén", 4.6946, -74.0302),
                "Salió corriendo cuando abrieron la puerta.",
                new Contacto("Andrés", "3109876543"),
                new Mascota("Max", "perro", "labrador", "café", "collar azul")));
        gestion.registrar(TipoReporte.ENCONTRADA, SolicitudReporte.paraEncontrada(
                new Ubicacion("Calle 147", "Portal de un edificio", 4.7303, -74.0460),
                "Apareció en el portal y la tengo en casa.",
                new Contacto("Lorenzi", "3007654321"),
                "Gata gris adulta, muy mansa, sin collar"));
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Suba", "Centro comercial Santafé", 4.7417, -74.0833),
                "Se perdió en el parqueadero.",
                new Contacto("Sofía", "sofia@correo.co"),
                new Mascota("Copito", "conejo", "", "blanco", "orejas caídas")));
    }
}
