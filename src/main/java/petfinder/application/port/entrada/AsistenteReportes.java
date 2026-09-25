package petfinder.application.port.entrada;

import java.util.List;

import petfinder.domain.model.BorradorReporte;

/**
 * Puerto de entrada del asistente: un turno de conversación sobre la tarjeta
 * viva. Es sin estado: el cliente envía el borrador que tiene y la frase
 * nueva, y recibe el borrador actualizado, lo que falta y la siguiente
 * pregunta. Publicar sigue siendo GestionReportes.registrar().
 */
public interface AsistenteReportes {

    ResultadoTurno procesarTurno(BorradorReporte actual, String texto);

    /**
     * @param fuente de dónde salieron los datos de este turno: "claude" o "regex"
     */
    record ResultadoTurno(BorradorReporte borrador,
                          List<BorradorReporte.Campo> faltantes,
                          String pregunta,
                          String fuente) {

        public boolean listoParaPublicar() {
            return faltantes.isEmpty();
        }
    }
}
