package petfinder.domain.observer;

import java.time.LocalDateTime;

import petfinder.domain.model.Avistamiento;

/**
 * Información enviada a los observadores cuando se registra correctamente
 * un avistamiento sobre una mascota perdida.
 *
 * El evento agrupa todo el contexto necesario para que cada observador pueda
 * reaccionar sin tener que consultar nuevamente el reporte o el repositorio.
 */
public record EventoAvistamiento(
        String idReporte,
        String nombreMascota,
        Avistamiento avistamiento,
        LocalDateTime fechaHora) {
}