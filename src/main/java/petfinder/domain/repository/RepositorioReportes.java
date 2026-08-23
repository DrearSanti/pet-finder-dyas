package petfinder.domain.repository;

import java.util.List;
import java.util.Optional;

import petfinder.domain.model.ReporteMascota;

/**
 * Contrato de almacenamiento de los reportes de mascotas.
 *
 * El dominio define las operaciones que necesita, pero no conoce cómo se
 * almacenan los datos. Una implementación puede usar memoria, archivos o una
 * base de datos sin obligar a modificar los servicios.
 */
public interface RepositorioReportes {

    /**
     * Almacena un reporte.
     *
     * @param reporte reporte que se desea guardar
     */
    void guardar(ReporteMascota reporte);

    /**
     * Busca un reporte utilizando su identificador.
     *
     * @param id identificador con formato como PF-001
     * @return el reporte encontrado o un Optional vacío si no existe
     */
    Optional<ReporteMascota> buscarPorId(String id);

    /**
     * Obtiene únicamente los reportes que todavía están activos.
     *
     * @return lista de reportes activos
     */
    List<ReporteMascota> listarActivos();
}