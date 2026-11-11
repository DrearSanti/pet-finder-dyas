package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import petfinder.domain.model.EstadoReporte;

/** Consultas de Spring Data. Solo la usa RepositorioReportesH2. */
public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, String> {

    List<ReporteEntity> findByEstadoOrderByFechaCreacionDesc(EstadoReporte estado);

    /** Solo los identificadores: al arrancar no hace falta cargar los reportes completos. */
    @Query("select r.id from ReporteEntity r")
    List<String> listarIds();
}
