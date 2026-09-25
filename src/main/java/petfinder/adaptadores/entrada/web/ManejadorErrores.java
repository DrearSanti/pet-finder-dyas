package petfinder.adaptadores.entrada.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.exception.DominioException;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;

/**
 * Único lugar donde un error se convierte en respuesta HTTP.
 *
 * Los controladores no llevan {@code try/catch}: dejan subir la excepción del
 * dominio y aquí se decide el código. Así la regla "qué estado significa qué"
 * está escrita una sola vez, y el mensaje que ve la persona es el mismo que
 * escribió el dominio en español, en el cuerpo {@code {"error": "..."}} que
 * {@code api.js} lee en el navegador.
 */
@RestControllerAdvice
public class ManejadorErrores {

    static final String CUERPO_INVALIDO = "El cuerpo de la petición no es válido";

    /** Forma única de todo error de la API. */
    public record CuerpoError(String error) {
    }

    /** Datos que no cumplen una regla del dominio: la persona puede corregirlos. */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<CuerpoError> datosInvalidos(DatosInvalidosException excepcion) {
        return responder(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(ReporteNoEncontradoException.class)
    public ResponseEntity<CuerpoError> noEncontrado(ReporteNoEncontradoException excepcion) {
        return responder(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    /** El reporte existe pero su estado no admite la acción: conflicto, no error de datos. */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<CuerpoError> operacionNoPermitida(OperacionNoPermitidaException excepcion) {
        return responder(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    /**
     * Red de seguridad para una subclase nueva de {@link DominioException} que
     * todavía no tenga su propio código: sigue siendo culpa de la petición y
     * conserva su mensaje, en vez de volverse un 500 sin explicación.
     */
    @ExceptionHandler(DominioException.class)
    public ResponseEntity<CuerpoError> otroErrorDeDominio(DominioException excepcion) {
        return responder(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    /**
     * JSON ilegible o con un valor imposible (por ejemplo un {@code tipo} que no
     * existe). El detalle técnico de Jackson no se muestra: no le sirve a la
     * persona y revela cómo está hecho el servidor.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CuerpoError> cuerpoIlegible(HttpMessageNotReadableException excepcion) {
        return responder(HttpStatus.BAD_REQUEST, CUERPO_INVALIDO);
    }

    private static ResponseEntity<CuerpoError> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new CuerpoError(mensaje));
    }
}
