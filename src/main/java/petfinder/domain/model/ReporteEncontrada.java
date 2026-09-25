package petfinder.domain.model;

import java.time.LocalDateTime;

/**
 * Caso abierto por alguien que encontró una mascota y la tiene bajo su
 * cuidado.
 *
 * A diferencia de una pérdida, aquí no hay un objeto Mascota: quien la
 * encontró no sabe su nombre ni su raza, solo puede describir lo que ve. Por
 * eso el campo es una descripción libre y no una entidad identificada.
 */
public class ReporteEncontrada extends ReporteMascota {

    private final String descripcionMascota;
    private final Contacto contactoReportante;

    public ReporteEncontrada(
            String id,
            Ubicacion ubicacion,
            String descripcion,
            String descripcionMascota,
            Contacto contactoReportante) {
        super(id, ubicacion, descripcion);
        this.descripcionMascota = descripcionMascota;
        this.contactoReportante = contactoReportante;
    }

    private ReporteEncontrada(String id, Ubicacion ubicacion, String descripcion,
                              String descripcionMascota, Contacto contactoReportante,
                              LocalDateTime fechaCreacion, EstadoReporte estado) {
        super(id, ubicacion, descripcion, fechaCreacion, estado);
        this.descripcionMascota = descripcionMascota;
        this.contactoReportante = contactoReportante;
    }

    /** Devuelve a memoria un hallazgo que ya existía, con su fecha y su estado originales. */
    public static ReporteEncontrada reconstruir(String id, Ubicacion ubicacion, String descripcion,
                                                String descripcionMascota, Contacto contactoReportante,
                                                LocalDateTime fechaCreacion, EstadoReporte estado) {
        return new ReporteEncontrada(id, ubicacion, descripcion, descripcionMascota,
                contactoReportante, fechaCreacion, estado);
    }

    public String getDescripcionMascota() {
        return descripcionMascota;
    }

    public Contacto getContactoReportante() {
        return contactoReportante;
    }

    @Override
    public String resumen() {
        return String.format(
                "[%s] ENCONTRADA - %s | Zona: %s | Estado: %s | Contacto: %s",
                getId(),
                descripcionMascota,
                getUbicacion().zonaOBarrio(),
                getEstado(),
                contactoReportante.nombre());
    }
}
