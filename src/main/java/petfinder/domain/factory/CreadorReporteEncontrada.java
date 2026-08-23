package petfinder.domain.factory;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;

/**
 * Creador concreto para los casos de mascota encontrada.
 *
 * No pide un objeto Mascota sino una descripción libre: quien encuentra un
 * animal no conoce su nombre ni su raza, solo puede describir lo que ve. Esa
 * diferencia de validación entre los dos creadores es la razón por la que el
 * patrón vale la pena aquí.
 */
public class CreadorReporteEncontrada extends CreadorReporte {

    public CreadorReporteEncontrada(GeneradorIdReportes generador) {
        super(generador);
    }

    @Override
    protected ReporteMascota crearReporte(SolicitudReporte solicitud, String id) {
        if (esVacio(solicitud.descripcionMascota())) {
            throw new DatosInvalidosException(
                    "Se requiere una descripción del animal encontrado");
        }

        return new ReporteEncontrada(
                id,
                solicitud.ubicacion(),
                solicitud.descripcion(),
                solicitud.descripcionMascota(),
                solicitud.contacto());
    }
}