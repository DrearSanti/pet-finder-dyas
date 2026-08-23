package petfinder.domain.exception;

/**
 * La solicitud llegó incompleta o con datos que no permiten construir un
 * reporte válido. La lanzan los creadores durante la validación, antes de
 * instanciar nada.
 */
public class DatosInvalidosException extends DominioException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}