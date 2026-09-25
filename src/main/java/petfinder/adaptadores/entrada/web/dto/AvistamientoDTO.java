package petfinder.adaptadores.entrada.web.dto;

import java.time.LocalDateTime;

import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Ubicacion;

/**
 * Cuerpo de {@code POST /api/reportes/{id}/avistamientos}: dónde y cómo se vio
 * a la mascota.
 *
 * El contacto es opcional a propósito: quien vio una mascota en la calle debe
 * poder avisar sin dejar sus datos. El id y la hora no vienen en el JSON porque
 * los decide el servidor, no el navegador.
 */
public record AvistamientoDTO(
        String zona,
        String referencia,
        Double latitud,
        Double longitud,
        String descripcion,
        String nombreContacto,
        String medioContacto) {

    /**
     * Crea el avistamiento del dominio con el id y la hora que asigna el
     * controlador. Sin nombre ni medio de contacto no se inventa un
     * {@link Contacto} vacío: queda {@code null}.
     */
    public Avistamiento aAvistamiento(String id, LocalDateTime fechaHora) {
        Ubicacion ubicacion = new Ubicacion(zona, referencia, latitud, longitud);
        boolean sinContacto = estaVacio(nombreContacto) && estaVacio(medioContacto);
        Contacto contacto = sinContacto ? null : new Contacto(nombreContacto, medioContacto);
        return new Avistamiento(id, fechaHora, ubicacion, descripcion, contacto);
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
