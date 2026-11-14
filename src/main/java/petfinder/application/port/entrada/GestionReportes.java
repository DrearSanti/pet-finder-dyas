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
 *
 * Desde S9 cada caso publicado tiene un código de gestión, y solo con él (o
 * con el código de administrador) se resuelve o se cierra. El cambio va en
 * dos pasos para que ningún PR quede en rojo: S9 agrega publicar, resolver y
 * cerrar con código; A9 pasa los controladores a esos métodos y borra los
 * tres sin código.
 */
public interface GestionReportes {

    /**
     * Publica el caso y devuelve su código de gestión en claro, la única vez
     * que sale del sistema.
     */
    ReportePublicado publicar(TipoReporte tipo, SolicitudReporte solicitud);

    /**
     * Publica sin código de gestión.
     *
     * @deprecated se borra en A9: mientras exista, POST /api/reportes publica
     *             casos que cualquiera puede cerrar. Usar {@link #publicar}.
     */
    @Deprecated
    ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud);

    List<ReporteMascota> listarActivos();

    ReporteMascota consultar(String id);

    /**
     * Marca el caso como resuelto si el código es el suyo o el de
     * administrador; si no, CodigoGestionInvalidoException.
     */
    void resolver(String id, String codigo);

    /**
     * Cierra el caso si el código es el suyo o el de administrador; si no,
     * CodigoGestionInvalidoException.
     */
    void cerrar(String id, String codigo);

    /**
     * Resuelve sin pedir código.
     *
     * @deprecated se borra en A9, junto con el controlador que lo usa.
     */
    @Deprecated
    void resolver(String id);

    /**
     * Cierra sin pedir código.
     *
     * @deprecated se borra en A9, junto con el controlador que lo usa.
     */
    @Deprecated
    void cerrar(String id);
}
