package petfinder.application.port.entrada;

import java.util.List;

import petfinder.domain.model.Contacto;
import petfinder.domain.model.ReporteMascota;

/**
 * Puerto de entrada del reto de captura de voz: recibe la frase ya transcrita
 * (el navegador convierte el audio en texto; el servidor nunca recibe audio)
 * y devuelve qué se entendió y qué se hizo.
 *
 * El contacto viaja aparte porque una frase dicha en voz alta no debería
 * llevar un número de teléfono: la página lo toma del formulario.
 */
public interface ProcesadorComandosVoz {

    ResultadoComandoVoz procesar(String texto, Contacto contacto);

    /**
     * @param reportes      los reportes que tocó el comando: el creado, el
     *                      consultado o los activos; vacío si no se entendió
     * @param codigoGestion el código de gestión del caso recién publicado (S9),
     *                      la única vez que sale; null en cualquier otro comando
     */
    record ResultadoComandoVoz(AccionVoz accion, String mensaje, List<ReporteMascota> reportes,
                               String codigoGestion) {

        /** Comandos que no publican: no hay código que devolver. */
        public ResultadoComandoVoz(AccionVoz accion, String mensaje, List<ReporteMascota> reportes) {
            this(accion, mensaje, reportes, null);
        }

        @Override
        public String toString() {
            return "ResultadoComandoVoz[" + accion + ", " + mensaje + ", código "
                    + (codigoGestion == null ? "ninguno" : "oculto") + "]";
        }
    }

    enum AccionVoz { REGISTRAR_PERDIDA, REGISTRAR_ENCONTRADA, LISTAR_ACTIVOS, CONSULTAR, NO_RECONOCIDO }
}
