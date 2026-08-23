package petfinder.infrastructure.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import petfinder.domain.model.ReporteMascota;
import petfinder.domain.repository.RepositorioReportes;
/**
 * Implementación del repositorio que almacena los reportes en memoria.
 *
 * Utiliza el identificador de cada reporte como llave. Los datos existen
 * únicamente mientras la aplicación está ejecutándose, lo cual es suficiente
 * para la demostración por terminal de este corte.
 */
public class RepositorioReportesEnMemoria implements RepositorioReportes {

    private final Map<String, ReporteMascota> reportes = new LinkedHashMap<>();

    /**
     * Guarda un reporte nuevo o reemplaza la versión almacenada de uno
     * existente que tenga el mismo identificador.
     */
    @Override
    public void guardar(ReporteMascota reporte) {
        reportes.put(reporte.getId(), reporte);
    }

    /**
     * Busca directamente mediante la llave del mapa.
     */
    @Override
    public Optional<ReporteMascota> buscarPorId(String id) {
        return Optional.ofNullable(reportes.get(id));
    }

    /**
     * Construye una lista nueva que contiene solamente reportes activos.
     */
    @Override
    public List<ReporteMascota> listarActivos() {
        return reportes.values().stream()
                .filter(ReporteMascota::estaActivo)
                .toList();
    }
}