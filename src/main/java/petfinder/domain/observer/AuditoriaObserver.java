package petfinder.domain.observer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Observador concreto que conserva un registro en memoria de los eventos de
 * avistamiento recibidos.
 *
 * A diferencia de la alerta, este observador no imprime inmediatamente. Su
 * registro puede consultarse después desde la demostración.
 */
public class AuditoriaObserver implements ObservadorAvistamiento {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> registro = new ArrayList<>();

    /**
     * Convierte el evento recibido en una línea de auditoría y la almacena.
     */
    @Override
    public void actualizar(EventoAvistamiento evento) {
        String linea = String.format(
                "[%s] Reporte: %s | Mascota: %s | Avistamiento: %s | Zona: %s",
                evento.fechaHora().format(FORMATO_FECHA),
                evento.idReporte(),
                evento.nombreMascota(),
                evento.avistamiento().id(),
                evento.avistamiento().ubicacion().zonaOBarrio());

        registro.add(linea);
    }

    /**
     * Devuelve una copia de solo lectura del registro acumulado.
     *
     * @return líneas generadas por los eventos recibidos
     */
    public List<String> getRegistro() {
        return List.copyOf(registro);
    }
}