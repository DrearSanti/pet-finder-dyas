package petfinder.application.service;

import petfinder.application.port.entrada.GestionReportes;

import java.util.List;
import java.util.Map;

import petfinder.application.port.entrada.ReportePublicado;
import petfinder.domain.exception.CodigoGestionInvalidoException;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.model.CodigoGestion;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.application.port.salida.RepositorioReportes;

/**
 * Casos de uso relacionados con el ciclo de vida de un reporte: registrarlo,
 * consultarlo y cerrar su atención.
 *
 * No construye reportes ni decide cómo se almacenan: delega la creación en el
 * creador correspondiente y el almacenamiento en el repositorio. Su única
 * responsabilidad es coordinar ese flujo y traducir los fallos en excepciones
 * de dominio, que es la evidencia de SRP en esta capa.
 */
public class ServicioReportes implements GestionReportes {

    private final RepositorioReportes repositorio;
    private final Map<TipoReporte, CreadorReporte> creadores;

    /**
     * Huella del código de administrador, o null si no hay administrador.
     * Llega ya calculada desde config: el servicio nunca ve el código en claro.
     */
    private final String huellaAdministrador;

    /**
     * Recibe sus dos colaboradores ya construidos. El servicio depende de la
     * interfaz RepositorioReportes y de la abstracción CreadorReporte, nunca
     * de RepositorioReportesEnMemoria ni de los creadores concretos: esa es la
     * evidencia de DIP que se documenta en el README.
     */
    public ServicioReportes(RepositorioReportes repositorio,
                            Map<TipoReporte, CreadorReporte> creadores) {
        this(repositorio, creadores, null);
    }

    /**
     * Con administrador: su código resuelve o cierra cualquier caso, incluidos
     * los viejos que no tienen código. Una huella vacía cuenta como "sin
     * administrador", porque un código vacío nunca debe abrir nada.
     */
    public ServicioReportes(RepositorioReportes repositorio,
                            Map<TipoReporte, CreadorReporte> creadores,
                            String huellaAdministrador) {
        this.repositorio = repositorio;
        this.creadores = Map.copyOf(creadores);
        this.huellaAdministrador = huellaAdministrador == null || huellaAdministrador.isBlank()
                ? null : huellaAdministrador;
    }

    /**
     * Publica el caso con un código de gestión nuevo. El reporte se guarda
     * solo con la huella; el código en claro vuelve una vez, para quien publica.
     */
    @Override
    public ReportePublicado publicar(TipoReporte tipo, SolicitudReporte solicitud) {
        ReporteMascota reporte = crear(tipo, solicitud);
        CodigoGestion codigo = CodigoGestion.generar();
        reporte.protegerConHuella(codigo.huella());
        repositorio.guardar(reporte);
        return new ReportePublicado(reporte, codigo.valor());
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
    @Override
    @Deprecated
    public ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud) {
        ReporteMascota reporte = crear(tipo, solicitud);
        repositorio.guardar(reporte);
        return reporte;
    }

    private ReporteMascota crear(TipoReporte tipo, SolicitudReporte solicitud) {
        CreadorReporte creador = creadores.get(tipo);
        if (creador == null) {
            throw new OperacionNoPermitidaException(
                    "No hay un creador registrado para el tipo de reporte " + tipo);
        }
        return creador.preparar(solicitud);
    }

    public List<ReporteMascota> listarActivos() {
        return repositorio.listarActivos();
    }

    /** Consulta puntual de un caso, para RF-04. */
    public ReporteMascota consultar(String id) {
        return buscarOFallar(id);
    }

    @Override
    public void resolver(String id, String codigo) {
        ReporteMascota reporte = buscarConCodigo(id, codigo);
        reporte.resolver();
        repositorio.guardar(reporte);
    }

    @Override
    public void cerrar(String id, String codigo) {
        ReporteMascota reporte = buscarConCodigo(id, codigo);
        reporte.cerrar();
        repositorio.guardar(reporte);
    }

    @Override
    @Deprecated
    public void resolver(String id) {
        ReporteMascota reporte = buscarOFallar(id);
        reporte.resolver();
        repositorio.guardar(reporte);
    }

    @Override
    @Deprecated
    public void cerrar(String id) {
        ReporteMascota reporte = buscarOFallar(id);
        reporte.cerrar();
        repositorio.guardar(reporte);
    }

    /**
     * Primero si existe (404) y después si el código sirve (403): un caso que
     * no existe no tiene código que comparar. El mismo error para "sin código"
     * y "código de otro caso", para no dar pistas a quien prueba códigos.
     */
    private ReporteMascota buscarConCodigo(String id, String codigo) {
        ReporteMascota reporte = buscarOFallar(id);
        boolean esAdministrador = CodigoGestion.coincide(codigo, huellaAdministrador);
        if (!reporte.admiteCodigo(codigo) && !esAdministrador) {
            throw new CodigoGestionInvalidoException();
        }
        return reporte;
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