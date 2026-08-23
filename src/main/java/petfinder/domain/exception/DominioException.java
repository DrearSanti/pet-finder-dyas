package petfinder.domain.exception;

/**
 * Raíz de los errores que representan una violación de las reglas del negocio,
 * no una falla técnica.
 *
 * Es abstracta a propósito: obliga a lanzar siempre una de las subclases, que
 * dicen qué regla se rompió. Extiende RuntimeException porque quien invoca un
 * caso de uso no puede hacer nada útil con estos errores salvo mostrarlos;
 * declararlos en cada firma solo llenaría el código de throws.
 */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensaje) {
        super(mensaje);
    }
}