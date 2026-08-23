package petfinder.application;

import java.util.List;
import java.util.Map;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.repository.RepositorioReportes;

/**
 * Casos de uso relacionados con el ciclo de vida de un reporte: registrarlo,
 * consultarlo y cerrar su atención.
 *
 * No construye reportes ni decide cómo se almacenan: delega la creación en el
 * creador correspondiente y el almacenamiento en el repositorio. Su única
 * responsabilidad es coordinar ese flujo y traducir los fallos en excepciones
 * de dominio, que es la evidencia de SRP en esta capa.
 */
public class ServicioReportes {

    private final RepositorioReportes repositorio;
    private final Map<TipoReporte, CreadorReporte> creadores;

    /**
     * Recibe sus dos colaboradores ya construidos. El servicio depende de la
     * interfaz RepositorioReportes y de la abstracción CreadorReporte, nunca
     * de RepositorioReportesEnMemoria ni de los creadores concretos: esa es la
     * evidencia de DIP que se documenta en el README.
     */
    public ServicioReportes(RepositorioReportes repositorio,
                            Map<TipoReporte, CreadorReporte> creadores) {
        this.repositorio = repositorio;
        this.creadores = Map.copyOf(creadores);
    }

    /**
     * Registra un reporte del tipo indicado.
     *
     * Recibe el enum y no el creador ya construido a propósito: si la consola
     * tuviera que elegir la instancia, el condicional que Factory Method
     * elimina reaparecería en la interfaz de usuario. Aquí el tipo es solo una
     * llave de búsqueda y el servicio nunca nombra una clase concreta de
     * reporte.
     */
    public ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud) {
        CreadorReporte creador = creadores.get(tipo);
        if (creador == null) {
            throw new OperacionNoPermitidaException(
                    "No hay un creador registrado para el tipo de reporte " + tipo);
        }
        ReporteMascota reporte = creador.preparar(solicitud);
        repositorio.guardar(reporte);
        return reporte;
    }

    public List<ReporteMascota> listarActivos() {
        return repositorio.listarActivos();
    }

    /** Consulta puntual de un caso, para RF-04. */
    public ReporteMascota consultar(String id) {
        return buscarOFallar(id);
    }

    public void resolver(String id) {
        ReporteMascota reporte = buscarOFallar(id);
        reporte.resolver();
        repositorio.guardar(reporte);
    }

    public void cerrar(String id) {
        ReporteMascota reporte = buscarOFallar(id);
        reporte.cerrar();
        repositorio.guardar(reporte);
    }

    /**
     * Concentra en un solo lugar la traducción de "no está" a excepción de
     * dominio. Si esto se repitiera en cada método, agregar un caso de uso
     * nuevo sería otra oportunidad de olvidar la validación.
     */
    private ReporteMascota buscarOFallar(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReporteNoEncontradoException(id));
    }
}