package petfinder.application;

import java.time.LocalDateTime;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.observer.EventoAvistamiento;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.domain.repository.RepositorioReportes;

/**
 * Caso de uso de registrar un avistamiento sobre un reporte de pérdida activo.
 *
 * Se separa de ServicioReportes porque tiene una razón de cambio distinta:
 * aquí viven las reglas de qué reporte admite avistamientos y la publicación
 * del evento, no el ciclo de vida del reporte. Esa separación es parte de la
 * evidencia de SRP documentada en el README.
 */
public class ServicioAvistamientos {

    private final RepositorioReportes repositorio;
    private final PublicadorAvistamientos publicador;

    public ServicioAvistamientos(RepositorioReportes repositorio,
                                 PublicadorAvistamientos publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    /**
     * El orden de las operaciones es una regla, no una casualidad: si el
     * reporte no existe, no es una pérdida o no está activo, la excepción
     * corta el flujo ANTES de guardar y ANTES de publicar. Nunca se notifica
     * a nadie por una operación que falló.
     */
    public void registrar(String idReporte, Avistamiento avistamiento) {
        ReportePerdida reporte = obtenerPerdidaActiva(idReporte);
        reporte.agregarAvistamiento(avistamiento);
        repositorio.guardar(reporte);
        publicador.notificar(new EventoAvistamiento(
                reporte.getId(),
                reporte.getMascota().nombre(),
                avistamiento,
                LocalDateTime.now()));
    }

    /**
     * Único punto del proyecto donde se pregunta por el tipo concreto de un
     * reporte. Se concentra aquí a propósito: repartir instanceof por varios
     * métodos convertiría cada nuevo tipo de reporte en una cacería de
     * condicionales, que es lo que Factory Method evita en la creación.
     */
    private ReportePerdida obtenerPerdidaActiva(String idReporte) {
        ReporteMascota reporte = repositorio.buscarPorId(idReporte)
                .orElseThrow(() -> new ReporteNoEncontradoException(idReporte));

        if (!(reporte instanceof ReportePerdida perdida)) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + idReporte
                            + " no es de tipo PERDIDA y no admite avistamientos");
        }
        if (!perdida.estaActivo()) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + idReporte
                            + " no está activo y no admite avistamientos");
        }
        return perdida;
    }
}