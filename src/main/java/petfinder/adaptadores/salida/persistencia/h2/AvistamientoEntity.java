package petfinder.adaptadores.salida.persistencia.h2;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Fila de la tabla AVISTAMIENTOS. El contacto es opcional, como en el dominio. */
@Entity
@Table(name = "AVISTAMIENTOS")
public class AvistamientoEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_id")
    private ReporteEntity reporte;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String zona;
    private String referencia;
    private Double latitud;
    private Double longitud;

    @Column(length = 2000)
    private String descripcion;

    private String contactoNombre;
    private String contactoMedio;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ReporteEntity getReporte() { return reporte; }
    public void setReporte(ReporteEntity reporte) { this.reporte = reporte; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getContactoNombre() { return contactoNombre; }
    public void setContactoNombre(String contactoNombre) { this.contactoNombre = contactoNombre; }
    public String getContactoMedio() { return contactoMedio; }
    public void setContactoMedio(String contactoMedio) { this.contactoMedio = contactoMedio; }
}
