package petfinder.domain.factory;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;

/**
 * Creador concreto para los casos de mascota perdida.
 *
 * Exige que la solicitud traiga la mascota identificada: quien la perdió sabe
 * cómo se llama y qué aspecto tiene, y sin ese dato el reporte no sirve para
 * que nadie la reconozca en la calle.
 */
public class CreadorReportePerdida extends CreadorReporte {

    public CreadorReportePerdida(GeneradorIdReportes generador) {
        super(generador);
    }

    @Override
    protected ReporteMascota crearReporte(SolicitudReporte solicitud, String id) {
        if (solicitud.mascota() == null) {
            throw new DatosInvalidosException(
                    "Un reporte de pérdida requiere los datos de la mascota");
        }
        if (esVacio(solicitud.mascota().nombre())) {
            throw new DatosInvalidosException(
                    "Se requiere el nombre de la mascota perdida");
        }
        if (esVacio(solicitud.mascota().especie())) {
            throw new DatosInvalidosException(
                    "Se requiere la especie de la mascota perdida");
        }

        return new ReportePerdida(
                id,
                solicitud.ubicacion(),
                solicitud.descripcion(),
                solicitud.mascota(),
                solicitud.contacto());
    }
}