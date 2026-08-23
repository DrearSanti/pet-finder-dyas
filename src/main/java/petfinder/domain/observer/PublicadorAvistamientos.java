package petfinder.domain.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Publicador del patrón Observer para los eventos de avistamiento.
 *
 * Mantiene una colección de observadores y les entrega cada evento sin
 * conocer las clases concretas que recibirán la notificación.
 */
public class PublicadorAvistamientos {

    private final List<ObservadorAvistamiento> observadores = new ArrayList<>();

    /**
     * Agrega un observador si todavía no está suscrito.
     *
     * @param observador objeto interesado en recibir eventos
     */
    public void suscribir(ObservadorAvistamiento observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Retira un observador de la colección.
     *
     * @param observador objeto que dejará de recibir eventos
     */
    public void desuscribir(ObservadorAvistamiento observador) {
        observadores.remove(observador);
    }

    /**
     * Entrega el evento a todos los observadores que continúan suscritos.
     *
     * @param evento información del avistamiento registrado
     */
    public void notificar(EventoAvistamiento evento) {
        for (ObservadorAvistamiento observador : observadores) {
            observador.actualizar(evento);
        }
    }
}