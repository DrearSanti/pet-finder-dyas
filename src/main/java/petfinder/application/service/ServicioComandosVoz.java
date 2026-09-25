package petfinder.application.service;

import java.util.List;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.ProcesadorComandosVoz;
import petfinder.application.service.InterpreteComandoVoz.Comando;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Ejecuta lo que el intérprete entendió de una frase.
 *
 * Depende de GestionReportes, el mismo puerto que usan la consola y el
 * formulario, y no del repositorio: así un reporte dictado pasa por el mismo
 * Factory Method y las mismas validaciones que uno escrito. La voz es una
 * entrada más, no un camino paralelo con reglas propias.
 */
public class ServicioComandosVoz implements ProcesadorComandosVoz {

    static final String MENSAJE_NO_RECONOCIDO = "No entendí. Prueba con: perdí un perro llamado Max en Chía";

    /** La voz no da una referencia precisa; esto deja claro de dónde vino el dato. */
    static final String REFERENCIA_VOZ = "Reportado por voz";

    /** Raza, color y señas no se dictan en el comando corto; se completan después. */
    static final String NO_INDICADO = "No indicado";

    private final InterpreteComandoVoz interprete;
    private final GestionReportes gestionReportes;

    public ServicioComandosVoz(InterpreteComandoVoz interprete, GestionReportes gestionReportes) {
        this.interprete = interprete;
        this.gestionReportes = gestionReportes;
    }

    /**
     * La frase completa se guarda como descripción del reporte: es lo que la
     * persona dijo con sus palabras y le sirve a quien lo lea.
     */
    @Override
    public ResultadoComandoVoz procesar(String texto, Contacto contacto) {
        Comando comando = interprete.interpretar(texto);
        return switch (comando.accion()) {
            case REGISTRAR_PERDIDA -> registrarPerdida(comando, texto.strip(), contacto);
            case REGISTRAR_ENCONTRADA -> registrarEncontrada(comando, texto.strip(), contacto);
            case LISTAR_ACTIVOS -> listarActivos();
            case CONSULTAR -> consultar(comando.idReporte());
            case NO_RECONOCIDO -> new ResultadoComandoVoz(AccionVoz.NO_RECONOCIDO, MENSAJE_NO_RECONOCIDO, List.of());
        };
    }

    private ResultadoComandoVoz registrarPerdida(Comando comando, String frase, Contacto contacto) {
        Mascota mascota = new Mascota(comando.nombre(), comando.especie(), NO_INDICADO, NO_INDICADO, NO_INDICADO);
        SolicitudReporte solicitud = SolicitudReporte.paraPerdida(
                new Ubicacion(comando.zona(), REFERENCIA_VOZ), frase, contacto, mascota);
        ReporteMascota reporte = gestionReportes.registrar(TipoReporte.PERDIDA, solicitud);
        return new ResultadoComandoVoz(AccionVoz.REGISTRAR_PERDIDA,
                "Registré la pérdida de " + comando.nombre() + " en " + comando.zona()
                        + " con el código " + reporte.getId() + ".",
                List.of(reporte));
    }

    private ResultadoComandoVoz registrarEncontrada(Comando comando, String frase, Contacto contacto) {
        SolicitudReporte solicitud = SolicitudReporte.paraEncontrada(
                new Ubicacion(comando.zona(), REFERENCIA_VOZ), frase, contacto, comando.descripcion());
        ReporteMascota reporte = gestionReportes.registrar(TipoReporte.ENCONTRADA, solicitud);
        return new ResultadoComandoVoz(AccionVoz.REGISTRAR_ENCONTRADA,
                "Registré el hallazgo en " + comando.zona() + " con el código " + reporte.getId() + ".",
                List.of(reporte));
    }

    private ResultadoComandoVoz listarActivos() {
        List<ReporteMascota> activos = gestionReportes.listarActivos();
        String mensaje = switch (activos.size()) {
            case 0 -> "No hay reportes activos.";
            case 1 -> "Hay 1 reporte activo.";
            default -> "Hay " + activos.size() + " reportes activos.";
        };
        return new ResultadoComandoVoz(AccionVoz.LISTAR_ACTIVOS, mensaje, activos);
    }

    /**
     * Si el reporte no existe, sale la misma ReporteNoEncontradoException que
     * con el formulario: la API la convierte en el mismo 404 para las dos
     * entradas, en vez de que la voz invente su propio manejo.
     */
    private ResultadoComandoVoz consultar(String id) {
        ReporteMascota reporte = gestionReportes.consultar(id);
        return new ResultadoComandoVoz(AccionVoz.CONSULTAR, reporte.resumen(), List.of(reporte));
    }
}
