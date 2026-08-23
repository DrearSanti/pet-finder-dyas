package petfinder.domain.exception;

/**
 * Se pidió operar sobre un reporte cuyo identificador no existe en el
 * repositorio. Recibe el id directamente para armar un mensaje que le sirva
 * al usuario, en vez de un texto genérico.
 */
public class ReporteNoEncontradoException extends DominioException {

    public ReporteNoEncontradoException(String idReporte) {
        super("No existe un reporte con el identificador " + idReporte);
    }
}