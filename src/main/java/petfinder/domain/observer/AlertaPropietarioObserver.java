package petfinder.domain.observer;

/**
 * Observador concreto que simula una alerta al propietario por medio de la
 * consola.
 *
 * No consulta el repositorio ni modifica el reporte: solamente utiliza la
 * información recibida en el evento para construir el mensaje.
 */
public class AlertaPropietarioObserver implements ObservadorAvistamiento {

    /**
     * Muestra una alerta cada vez que el publicador entrega un nuevo evento.
     */
    @Override
    public void actualizar(EventoAvistamiento evento) {
        System.out.println();
        System.out.println("  >>> ALERTA AL PROPIETARIO <<<");
        System.out.println("  Nuevo avistamiento de " + evento.nombreMascota());
        System.out.println("  Reporte: " + evento.idReporte());
        System.out.println(
                "  Lugar: " + evento.avistamiento().ubicacion().zonaOBarrio()
                        + " - " + evento.avistamiento().ubicacion().referencia());
        System.out.println(
                "  Descripcion: " + evento.avistamiento().descripcion());
    }
}