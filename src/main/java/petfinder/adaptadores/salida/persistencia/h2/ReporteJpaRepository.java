package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import petfinder.domain.model.EstadoReporte;

/** Consultas de Spring Data. Solo la usa RepositorioReportesH2. */
public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, String> {

    /**
     * Los reportes de un estado, los más recientes primero, con sus
     * avistamientos en la misma consulta.
     *
     * Antes la lista hacía 1 + N consultas: una por los reportes y otra por los
     * avistamientos de cada uno, porque la colección es perezosa. El tope va en
     * la subconsulta de identificadores y no en un Pageable: con un join fetch
     * de una colección, Hibernate pagina en memoria (aviso HHH90003004) y
     * trae todas las filas igual.
     */
    @Query("""
            select r from ReporteEntity r
            left join fetch r.avistamientos
            where r.id in (
                select r2.id from ReporteEntity r2
                where r2.estado = :estado
                order by r2.fechaCreacion desc
                limit :maximo)
            order by r.fechaCreacion desc
            """)
    List<ReporteEntity> recientesConAvistamientos(@Param("estado") EstadoReporte estado, @Param("maximo") int maximo);

    /** Solo los identificadores: al arrancar no hace falta cargar los reportes completos. */
    @Query("select r.id from ReporteEntity r")
    List<String> listarIds();
}
