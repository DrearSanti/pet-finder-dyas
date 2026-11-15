package petfinder.domain.exception;

/**
 * Se intentó resolver o cerrar un caso sin su código de gestión, o con el de
 * otro caso. El mensaje no dice cuál de las dos cosas pasó: así no sirve para
 * probar códigos hasta acertar.
 */
public class CodigoGestionInvalidoException extends DominioException {

    public CodigoGestionInvalidoException() {
        super("Ese código no corresponde a este caso. Revisa el código que recibiste al publicar.");
    }
}
