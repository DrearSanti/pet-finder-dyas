package petfinder.domain.model;

import java.time.LocalDateTime;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Aviso de alguien sobre una mascota buscada: que la vio (LA_VI) o que la
 * tiene bajo su cuidado (LA_TENGO).
 *
 * Se distingue de un reporte de mascota encontrada: queda pegado al caso de
 * la mascota, así la familia lo ve sin buscar otro reporte. En una pista el
 * contacto es opcional porque un vecino puede querer avisar sin dejar sus
 * datos; en un hallazgo es obligatorio, porque sin él nadie puede ir por ella.
 */
public record Avistamiento(
        String id,
        LocalDateTime fechaHora,
        Ubicacion ubicacion,
        String descripcion,
        Contacto contactoReportante,
        TipoAvistamiento tipo) {

    public Avistamiento {
        if (tipo == null) {
            tipo = TipoAvistamiento.LA_VI;
        }
        if (tipo == TipoAvistamiento.LA_TENGO && !tieneMedio(contactoReportante)) {
            throw new DatosInvalidosException(
                    "Si la tienes contigo, deja tu contacto para que su familia te encuentre.");
        }
    }

    /** Una pista, como antes de existir los hallazgos: así el código que ya la creaba no cambia. */
    public Avistamiento(String id, LocalDateTime fechaHora, Ubicacion ubicacion, String descripcion,
                        Contacto contactoReportante) {
        this(id, fechaHora, ubicacion, descripcion, contactoReportante, TipoAvistamiento.LA_VI);
    }

    /** Verdadero si quien avisa tiene a la mascota bajo su cuidado. */
    public boolean esHallazgo() {
        return tipo == TipoAvistamiento.LA_TENGO;
    }

    private static boolean tieneMedio(Contacto contacto) {
        return contacto != null && contacto.medioContacto() != null && !contacto.medioContacto().isBlank();
    }
}
