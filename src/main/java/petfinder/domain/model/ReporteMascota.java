package petfinder.domain.model;

import java.time.LocalDateTime;

import petfinder.domain.exception.OperacionNoPermitidaException;

/**
 * Base común de todo reporte: identificacion, dónde y cuándo ocurrió, y en
 * qué estado está el caso.
 *
 * Es abstracta porque un reporte "en general" no existe en el dominio; existen
 * pérdidas y hallazgos, y cada uno se resume de forma distinta. Ese resumen es
 * el único método que las subclases deben implementar.
 *
 * El estado se cambia únicamente a través de resolver() y cerrar(), nunca con
 * un setter: así ninguna clase externa puede saltarse las reglas de
 * transición.
 */
public abstract class ReporteMascota {

    private final String id;
    private final LocalDateTime fechaCreacion;
    private final Ubicacion ubicacion;
    private final String descripcion;
    private EstadoReporte estado;

    /**
     * Huella del código de gestión (S9). Nula en los casos publicados antes
     * del código: esos solo los cierra el administrador.
     */
    private String huellaCodigo;

    protected ReporteMascota(String id, Ubicacion ubicacion, String descripcion) {
        this(id, ubicacion, descripcion, LocalDateTime.now(), EstadoReporte.ACTIVO);
    }

    /**
     * Constructor de reconstrucción: lo usan los métodos reconstruir() de las
     * subclases cuando un adaptador de persistencia devuelve un reporte que ya
     * existía. Conserva la fecha y el estado originales en lugar de pisarlos
     * con "ahora" y ACTIVO.
     */
    protected ReporteMascota(String id, Ubicacion ubicacion, String descripcion,
                             LocalDateTime fechaCreacion, EstadoReporte estado) {
        this.id = id;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    public void resolver() {
        cambiarEstado(EstadoReporte.RESUELTO);
    }

    public void cerrar() {
        cambiarEstado(EstadoReporte.CERRADO);
    }

    /**
     * Punto único de cambio de estado. Delega la regla en el propio enum, de
     * modo que agregar un estado nuevo no obliga a tocar esta clase.
     */
    private void cambiarEstado(EstadoReporte destino) {
        if (!estado.permiteTransicionA(destino)) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + id + " está en estado " + estado
                            + " y no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    /**
     * Fija la huella del código de gestión una sola vez: al publicar o al
     * reconstruir desde la base. No es un setter: una segunda llamada se
     * rechaza, porque cambiar la huella de un caso publicado le quitaría el
     * control a quien lo publicó.
     */
    public void protegerConHuella(String huella) {
        if (huella == null || huella.isBlank()) {
            throw new IllegalArgumentException("La huella del código de gestión no puede estar vacía");
        }
        if (this.huellaCodigo != null) {
            throw new OperacionNoPermitidaException("El reporte " + id + " ya tiene código de gestión");
        }
        this.huellaCodigo = huella;
    }

    /** Si el código recibido es el de este caso. Sin huella, ningún código lo es. */
    public boolean admiteCodigo(String codigoRecibido) {
        return CodigoGestion.coincide(codigoRecibido, huellaCodigo);
    }

    public String getHuellaCodigo() {
        return huellaCodigo;
    }

    public boolean estaActivo() {
        return estado == EstadoReporte.ACTIVO;
    }

    /** Cada tipo de reporte decide qué información mostrar. */
    public abstract String resumen();

    public String getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoReporte getEstado() {
        return estado;
    }
}
