package petfinder.application.port.entrada;

import java.util.List;

import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Puerto de entrada del ciclo de vida de un reporte.
 *
 * Es lo único que conocen los adaptadores de entrada (consola, web, voz,
 * asistente): ninguno nombra a ServicioReportes. Así agregar una forma nueva
 * de usar el sistema no toca el caso de uso.
 */
public interface GestionReportes {

    ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud);

    List<ReporteMascota> listarActivos();

    ReporteMascota consultar(String id);

    void resolver(String id);

    void cerrar(String id);
}
