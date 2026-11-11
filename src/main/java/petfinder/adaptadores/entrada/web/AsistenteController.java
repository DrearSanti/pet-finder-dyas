package petfinder.adaptadores.entrada.web;

import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.CORTO;
import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.LARGO;
import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.exigir;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import petfinder.application.port.entrada.AsistenteReportes;
import petfinder.application.port.entrada.AsistenteReportes.ResultadoTurno;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Un turno de conversación con el asistente que llena la tarjeta viva.
 *
 * El servidor no guarda la conversación: el navegador manda el borrador que
 * tiene y recibe el borrador fusionado. Así cualquier instancia puede atender
 * cualquier turno y no hay sesiones que limpiar. Este endpoint nunca publica;
 * cuando {@code listo} es verdadero, la persona revisa la tarjeta y la página
 * hace el {@code POST /api/reportes}.
 */
@RestController
public class AsistenteController {

    /** Cuerpo de la petición: borrador actual ({@code null} en el primer turno) y la frase nueva. */
    public record TurnoDTO(BorradorDTO borrador, String texto) {
    }

    /** Espejo JSON de {@link BorradorReporte}, campo por campo. */
    public record BorradorDTO(TipoReporte tipo, String nombre, String especie, String raza, String color,
                              String senas, String descripcionMascota, String zona, String referencia,
                              Double latitud, Double longitud, String descripcion,
                              String contactoNombre, String contactoMedio) {

        static BorradorDTO desde(BorradorReporte b) {
            return new BorradorDTO(b.tipo(), b.nombre(), b.especie(), b.raza(), b.color(), b.senas(),
                    b.descripcionMascota(), b.zona(), b.referencia(), b.latitud(), b.longitud(),
                    b.descripcion(), b.contactoNombre(), b.contactoMedio());
        }

        /**
         * Con los mismos topes de {@code POST /api/reportes}: la tarjeta que
         * se arma aquí es la que después se publica, y cada campo viaja a
         * Claude en el turno siguiente.
         */
        BorradorReporte aBorrador() {
            return new BorradorReporte(tipo, exigir(nombre, CORTO, "nombre de la mascota"),
                    exigir(especie, CORTO, "especie"), exigir(raza, CORTO, "raza"), exigir(color, CORTO, "color"),
                    exigir(senas, CORTO, "señas"), exigir(descripcionMascota, LARGO, "descripción de la mascota"),
                    exigir(zona, CORTO, "zona"), exigir(referencia, CORTO, "referencia"), latitud, longitud,
                    exigir(descripcion, LARGO, "descripción"), exigir(contactoNombre, CORTO, "nombre de contacto"),
                    exigir(contactoMedio, CORTO, "medio de contacto"));
        }
    }

    /** Borrador fusionado, qué falta, la siguiente pregunta, quién extrajo los datos y si ya se puede publicar. */
    public record RespuestaTurnoDTO(BorradorDTO borrador, List<BorradorReporte.Campo> faltantes,
                                    String pregunta, String fuente, boolean listo) {
    }

    private final AsistenteReportes asistente;

    public AsistenteController(AsistenteReportes asistente) {
        this.asistente = asistente;
    }

    @PostMapping("/api/asistente/turno")
    public RespuestaTurnoDTO turno(@RequestBody TurnoDTO turno) {
        BorradorReporte actual = turno.borrador() == null ? null : turno.borrador().aBorrador();
        ResultadoTurno resultado = asistente.procesarTurno(actual, turno.texto());
        return new RespuestaTurnoDTO(BorradorDTO.desde(resultado.borrador()), resultado.faltantes(),
                resultado.pregunta(), resultado.fuente(), resultado.listoParaPublicar());
    }
}
