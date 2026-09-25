package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import petfinder.application.port.salida.RepositorioReportes;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Adaptador de salida: implementa el puerto RepositorioReportes sobre H2.
 *
 * No lleva @Repository a propósito: lo construye ConfiguracionPetFinder, que
 * es quien decide qué implementación del puerto usa la aplicación. Si Spring
 * lo encontrara solo, habría dos beans del mismo puerto.
 *
 * El mapeo dominio ↔ entidad vive aquí, en un solo lugar; es el único sitio
 * del proyecto donde se pregunta por el tipo concreto de un reporte para
 * guardarlo.
 */
public class RepositorioReportesH2 implements RepositorioReportes {

    private final ReporteJpaRepository jpa;

    public RepositorioReportesH2(ReporteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void guardar(ReporteMascota reporte) {
        // Se reutiliza la fila existente y se limpia su lista: reemplazar la
        // colección provoca "A collection with cascade=all-delete-orphan was
        // no longer referenced".
        ReporteEntity fila = jpa.findById(reporte.getId()).orElseGet(ReporteEntity::new);
        copiar(reporte, fila);
        jpa.save(fila);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteMascota> buscarPorId(String id) {
        return jpa.findById(id).map(RepositorioReportesH2::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMascota> listarActivos() {
        return jpa.findByEstadoOrderByFechaCreacionDesc(EstadoReporte.ACTIVO).stream()
                .map(RepositorioReportesH2::aDominio)
                .toList();
    }

    private static void copiar(ReporteMascota reporte, ReporteEntity fila) {
        fila.setId(reporte.getId());
        fila.setEstado(reporte.getEstado());
        fila.setFechaCreacion(reporte.getFechaCreacion());
        fila.setDescripcion(reporte.getDescripcion());
        Ubicacion u = reporte.getUbicacion();
        fila.setZona(u.zonaOBarrio());
        fila.setReferencia(u.referencia());
        fila.setLatitud(u.latitud());
        fila.setLongitud(u.longitud());
        fila.getAvistamientos().clear();
        if (reporte instanceof ReportePerdida perdida) {
            fila.setTipo(TipoReporte.PERDIDA);
            Mascota m = perdida.getMascota();
            fila.setMascotaNombre(m.nombre());
            fila.setMascotaEspecie(m.especie());
            fila.setMascotaRaza(m.raza());
            fila.setMascotaColor(m.color());
            fila.setMascotaSenas(m.senasParticulares());
            fila.setContactoNombre(perdida.getContactoPropietario().nombre());
            fila.setContactoMedio(perdida.getContactoPropietario().medioContacto());
            for (Avistamiento a : perdida.getAvistamientos()) {
                fila.getAvistamientos().add(aFila(a, fila));
            }
        } else if (reporte instanceof ReporteEncontrada encontrada) {
            fila.setTipo(TipoReporte.ENCONTRADA);
            fila.setDescripcionMascota(encontrada.getDescripcionMascota());
            fila.setContactoNombre(encontrada.getContactoReportante().nombre());
            fila.setContactoMedio(encontrada.getContactoReportante().medioContacto());
        }
    }

    private static AvistamientoEntity aFila(Avistamiento a, ReporteEntity reporte) {
        AvistamientoEntity fila = new AvistamientoEntity();
        fila.setId(a.id());
        fila.setReporte(reporte);
        fila.setFechaHora(a.fechaHora());
        fila.setZona(a.ubicacion().zonaOBarrio());
        fila.setReferencia(a.ubicacion().referencia());
        fila.setLatitud(a.ubicacion().latitud());
        fila.setLongitud(a.ubicacion().longitud());
        fila.setDescripcion(a.descripcion());
        if (a.contactoReportante() != null) {
            fila.setContactoNombre(a.contactoReportante().nombre());
            fila.setContactoMedio(a.contactoReportante().medioContacto());
        }
        return fila;
    }

    private static ReporteMascota aDominio(ReporteEntity fila) {
        Ubicacion ubicacion = new Ubicacion(fila.getZona(), fila.getReferencia(), fila.getLatitud(), fila.getLongitud());
        Contacto contacto = new Contacto(fila.getContactoNombre(), fila.getContactoMedio());
        if (fila.getTipo() == TipoReporte.PERDIDA) {
            List<Avistamiento> avistamientos = fila.getAvistamientos().stream()
                    .map(RepositorioReportesH2::aAvistamiento)
                    .toList();
            return ReportePerdida.reconstruir(fila.getId(), ubicacion, fila.getDescripcion(),
                    new Mascota(fila.getMascotaNombre(), fila.getMascotaEspecie(), fila.getMascotaRaza(),
                            fila.getMascotaColor(), fila.getMascotaSenas()),
                    contacto, fila.getFechaCreacion(), fila.getEstado(), avistamientos);
        }
        return ReporteEncontrada.reconstruir(fila.getId(), ubicacion, fila.getDescripcion(),
                fila.getDescripcionMascota(), contacto, fila.getFechaCreacion(), fila.getEstado());
    }

    private static Avistamiento aAvistamiento(AvistamientoEntity fila) {
        Contacto contacto = fila.getContactoMedio() == null && fila.getContactoNombre() == null
                ? null
                : new Contacto(fila.getContactoNombre(), fila.getContactoMedio());
        return new Avistamiento(fila.getId(), fila.getFechaHora(),
                new Ubicacion(fila.getZona(), fila.getReferencia(), fila.getLatitud(), fila.getLongitud()),
                fila.getDescripcion(), contacto);
    }
}
