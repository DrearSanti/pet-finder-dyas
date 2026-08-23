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

    protected ReporteMascota(String id, Ubicacion ubicacion, String descripcion) {
        this.id = id;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReporte.ACTIVO;
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