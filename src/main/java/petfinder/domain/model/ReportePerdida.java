package petfinder.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import petfinder.domain.exception.OperacionNoPermitidaException;

/**
 * Caso abierto por alguien que perdió a su mascota y la está buscando.
 *
 * Es el único tipo de reporte que acumula avistamientos, porque solo tiene
 * sentido reportar dónde se vio a un animal que todavía nadie ha recuperado.
 */
public class ReportePerdida extends ReporteMascota {

    private final Mascota mascota;
    private final Contacto contactoPropietario;
    private final List<Avistamiento> avistamientos = new ArrayList<>();

    public ReportePerdida(
            String id,
            Ubicacion ubicacion,
            String descripcion,
            Mascota mascota,
            Contacto contactoPropietario) {
        super(id, ubicacion, descripcion);
        this.mascota = mascota;
        this.contactoPropietario = contactoPropietario;
    }

    /**
     * La verificación de estado se repite aquí aunque el servicio ya la haga.
     * La entidad no puede confiar en que quien la llame haya validado: si
     * mañana otro caso de uso agrega avistamientos, la regla sigue protegida.
     */
    public void agregarAvistamiento(Avistamiento avistamiento) {
        if (!estaActivo()) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + getId() + " ya no admite avistamientos");
        }
        avistamientos.add(avistamiento);
    }

    /**
     * Se devuelve una vista de solo lectura para que nadie agregue
     * avistamientos por fuera del método anterior, saltándose la validación.
     */
    public List<Avistamiento> getAvistamientos() {
        return Collections.unmodifiableList(avistamientos);
    }

    public Mascota getMascota() {
        return mascota;
    }

    public Contacto getContactoPropietario() {
        return contactoPropietario;
    }

    @Override
    public String resumen() {
        return String.format(
                "[%s] PERDIDA - %s (%s, %s) | Zona: %s | Estado: %s | Avistamientos: %d",
                getId(),
                mascota.nombre(),
                mascota.especie(),
                mascota.color(),
                getUbicacion().zonaOBarrio(),
                getEstado(),
                avistamientos.size());
    }
}