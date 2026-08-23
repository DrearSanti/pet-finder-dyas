package petfinder.domain.model;

import java.time.LocalDateTime;

/**
 * Reporte de que alguien vio una mascota buscada, sin tenerla bajo su cuidado.
 *
 * Se distingue de un reporte de mascota encontrada: aquí la persona solo
 * aporta una pista sobre dónde estaba el animal. El contacto es opcional
 * porque un vecino puede querer avisar sin dejar sus datos.
 */
public record Avistamiento(
        String id,
        LocalDateTime fechaHora,
        Ubicacion ubicacion,
        String descripcion,
        Contacto contactoReportante) {
}