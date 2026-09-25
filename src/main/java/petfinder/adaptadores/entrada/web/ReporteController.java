package petfinder.adaptadores.entrada.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import petfinder.adaptadores.entrada.web.dto.ReporteDTO;
import petfinder.adaptadores.entrada.web.dto.SolicitudReporteDTO;
import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.ReporteMascota;

/**
 * Ciclo de vida de un reporte por HTTP: publicarlo, listarlo, verlo, resolverlo
 * y cerrarlo.
 *
 * Hace con JSON lo mismo que {@code MenuConsola} hace con el teclado: traduce la
 * petición a una llamada al puerto {@link GestionReportes} y el resultado a un
 * DTO. Depende de la interfaz y no de {@code ServicioReportes} para que la web
 * no sepa quién implementa el caso de uso; y no captura excepciones porque de
 * eso se encarga {@link ManejadorErrores}.
 *
 * Es el único camino para publicar: el formulario, la voz confirmada y el
 * borrador del asistente terminan todos en {@code POST /api/reportes}.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final GestionReportes gestionReportes;

    public ReporteController(GestionReportes gestionReportes) {
        this.gestionReportes = gestionReportes;
    }

    /** Devuelve el detalle porque quien acaba de publicar necesita ver su caso completo. */
    @PostMapping
    public ResponseEntity<ReporteDTO> crear(@RequestBody SolicitudReporteDTO solicitud) {
        ReporteMascota reporte = gestionReportes.registrar(solicitud.tipoReporte(), solicitud.aSolicitud());
        return ResponseEntity.created(URI.create("/api/reportes/" + reporte.getId()))
                .body(ReporteDTO.detalle(reporte));
    }

    /** Lista pública: resumen con contacto enmascarado, en el orden del puerto (más reciente primero). */
    @GetMapping
    public List<ReporteDTO> listar() {
        return gestionReportes.listarActivos().stream().map(ReporteDTO::resumen).toList();
    }

    @GetMapping("/{id}")
    public ReporteDTO consultar(@PathVariable String id) {
        return ReporteDTO.detalle(gestionReportes.consultar(id));
    }

    @PostMapping("/{id}/resolver")
    public ResponseEntity<Void> resolver(@PathVariable String id) {
        gestionReportes.resolver(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable String id) {
        gestionReportes.cerrar(id);
        return ResponseEntity.noContent().build();
    }
}
