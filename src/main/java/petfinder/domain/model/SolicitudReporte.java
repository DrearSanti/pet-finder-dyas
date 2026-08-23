package petfinder.domain.model;

/**
 * Datos crudos que la interfaz recoge del usuario antes de que exista un
 * reporte. Es lo que recibe el creador para validar y construir el objeto.
 *
 * Los tres primeros campos aplican a cualquier tipo de reporte. Los dos
 * últimos son excluyentes: una pérdida trae la mascota identificada y deja
 * descripcionMascota en null, mientras que un hallazgo trae solo la
 * descripción de un animal que nadie ha identificado todavía.
 *
 * Se evaluó partir esto en dos clases, una por tipo de reporte, pero eso
 * obligaría a cada creador a verificar con instanceof qué solicitud le
 * llegó, que es justamente la verificación de tipo que el patrón evita.
 * El costo de esta decisión es que la clase tiene campos que no siempre se
 * usan; cada creador valida los suyos y deja los ajenos en null.
 */
public record SolicitudReporte(
        Ubicacion ubicacion,
        String descripcion,
        Contacto contacto,
        Mascota mascota,
        String descripcionMascota) {

    /** Atajo para armar una solicitud de mascota perdida. */
    public static SolicitudReporte paraPerdida(
            Ubicacion ubicacion,
            String descripcion,
            Contacto contactoPropietario,
            Mascota mascota) {
        return new SolicitudReporte(
                ubicacion, descripcion, contactoPropietario, mascota, null);
    }

    /** Atajo para armar una solicitud de mascota encontrada. */
    public static SolicitudReporte paraEncontrada(
            Ubicacion ubicacion,
            String descripcion,
            Contacto contactoReportante,
            String descripcionMascota) {
        return new SolicitudReporte(
                ubicacion, descripcion, contactoReportante, null, descripcionMascota);
    }
}