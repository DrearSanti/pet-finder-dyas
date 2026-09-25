package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import petfinder.domain.model.EstadoReporte;

/** Consultas de Spring Data. Solo la usa RepositorioReportesH2. */
public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, String> {

    List<ReporteEntity> findByEstadoOrderByFechaCreacionDesc(EstadoReporte estado);
}
