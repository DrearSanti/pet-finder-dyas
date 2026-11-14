package petfinder.adaptadores.entrada.web;

import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.CORTO;
import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.exigir;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import petfinder.adaptadores.entrada.web.ManejadorErrores.CuerpoError;
import petfinder.adaptadores.entrada.web.dto.ReporteDTO;
import petfinder.application.port.entrada.ProcesadorComandosVoz;
import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;
import petfinder.application.port.entrada.ProcesadorComandosVoz.ResultadoComandoVoz;
import petfinder.domain.model.Contacto;

/**
 * Comandos de voz: recibe el texto que ya transcribió el navegador (nunca
 * audio) y lo entrega al puerto {@link ProcesadorComandosVoz}.
 *
 * Es la prueba del reto de modificabilidad: un canal de entrada nuevo que no
 * tocó el dominio ni los servicios existentes. El código HTTP sale de la acción
 * que devolvió el puerto: 201 si se creó un reporte, 200 si solo se leyó, y 422
 * si la frase no se entendió, con el mismo cuerpo {@code {"error": ...}} de
 * cualquier otro error para que {@code api.js} lo trate igual.
 */
@RestController
public class VozController {

    /** Frase transcrita y, para registrar, el contacto de quien habla. */
    public record ComandoVozDTO(String texto, String nombreContacto, String medioContacto) {

        /**
         * La frase no necesita tope aquí: el intérprete ignora todo lo que pase
         * de {@code InterpreteComandoVoz.LONGITUD_MAXIMA}. El contacto sí, porque
         * se guarda tal cual en el reporte.
         */
        Contacto contacto() {
            return new Contacto(exigir(nombreContacto, CORTO, "nombre de contacto"),
                    exigir(medioContacto, CORTO, "medio de contacto"));
        }
    }

    /**
     * Qué entendió el sistema, qué respondió y los reportes involucrados (en
     * resumen). {@code codigoGestion} solo viene al publicar (S9): es la única
     * vez que se entrega, así que en los demás comandos ni aparece.
     */
    public record RespuestaVozDTO(AccionVoz accion, String mensaje, List<ReporteDTO> reportes,
                                  @JsonInclude(JsonInclude.Include.NON_NULL) String codigoGestion) {
    }

    /** 422: la petición llegó bien formada, pero la frase no corresponde a ningún comando. */
    private static final int NO_ENTENDIDO = 422;

    private final ProcesadorComandosVoz procesador;

    public VozController(ProcesadorComandosVoz procesador) {
        this.procesador = procesador;
    }

    @PostMapping("/api/voz")
    public ResponseEntity<?> procesar(@RequestBody ComandoVozDTO comando) {
        ResultadoComandoVoz resultado = procesador.procesar(comando.texto(), comando.contacto());

        if (resultado.accion() == AccionVoz.NO_RECONOCIDO) {
            return ResponseEntity.status(NO_ENTENDIDO).body(new CuerpoError(resultado.mensaje()));
        }
        RespuestaVozDTO respuesta = new RespuestaVozDTO(resultado.accion(), resultado.mensaje(),
                resultado.reportes().stream().map(ReporteDTO::resumen).toList(), resultado.codigoGestion());
        return ResponseEntity.status(creaReporte(resultado.accion()) ? HttpStatus.CREATED : HttpStatus.OK)
                .body(respuesta);
    }

    private static boolean creaReporte(AccionVoz accion) {
        return accion == AccionVoz.REGISTRAR_PERDIDA || accion == AccionVoz.REGISTRAR_ENCONTRADA;
    }
}
