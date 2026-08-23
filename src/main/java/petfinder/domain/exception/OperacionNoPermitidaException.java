package petfinder.domain.exception;

/**
 * El reporte existe, pero su estado o su tipo no admiten la operación
 * solicitada: cerrar un caso ya resuelto, o agregar un avistamiento a un
 * reporte de mascota encontrada.
 */
public class OperacionNoPermitidaException extends DominioException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}