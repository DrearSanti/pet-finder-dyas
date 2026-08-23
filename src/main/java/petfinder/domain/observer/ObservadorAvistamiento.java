package petfinder.domain.observer;

/**
 * Contrato que debe cumplir cualquier objeto interesado en recibir
 * notificaciones sobre nuevos avistamientos.
 *
 * El publicador trabaja únicamente con esta interfaz y no conoce las clases
 * concretas que reaccionarán al evento.
 */
public interface ObservadorAvistamiento {

    /**
     * Reacciona ante un avistamiento registrado correctamente.
     *
     * @param evento información del avistamiento que originó la notificación
     */
    void actualizar(EventoAvistamiento evento);
}