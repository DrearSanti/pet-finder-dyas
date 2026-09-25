package petfinder.application.port.salida;

import java.util.Optional;

import petfinder.domain.model.BorradorReporte;

/**
 * Puerto de salida que convierte una frase en datos de un reporte. Tiene dos
 * adaptadores intercambiables: uno con expresiones regulares, sin red, y uno
 * con Claude. El servicio los prueba en orden y usa el primero que responda.
 */
public interface ExtractorDatosReporte {

    /** Nombre corto que se muestra como fuente del turno: "claude" o "regex". */
    String nombre();

    /**
     * Devuelve solo lo que la frase menciona; lo demás queda en null. El
     * contexto es el borrador actual, para interpretar respuestas cortas como
     * un número de teléfono. Vacío si este extractor no pudo procesarla.
     */
    Optional<BorradorReporte> extraer(String texto, BorradorReporte contexto);
}
