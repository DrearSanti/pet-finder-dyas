package petfinder.adaptadores.salida.persistencia.h2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Fila de la tabla REPORTES. Es una clase distinta a ReporteMascota a
 * propósito: si el dominio llevara @Entity dependería de JPA y ArchUnit lo
 * rechazaría. Una sola tabla para los dos tipos; las columnas que no aplican
 * a un tipo quedan en null.
 */
@Entity
@Table(name = "REPORTES", indexes = @Index(name = "IDX_REPORTES_ESTADO", columnList = "estado"))
public class ReporteEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private String zona;
    private String referencia;
    private Double latitud;
    private Double longitud;

    @Column(length = 2000)
    private String descripcion;

    private String contactoNombre;
    private String contactoMedio;

    private String mascotaNombre;
    private String mascotaEspecie;
    private String mascotaRaza;
    private String mascotaColor;
    private String mascotaSenas;

    @Column(length = 2000)
    private String descripcionMascota;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    private List<AvistamientoEntity> avistamientos = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TipoReporte getTipo() { return tipo; }
    public void setTipo(TipoReporte tipo) { this.tipo = tipo; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
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
    public String getMascotaNombre() { return mascotaNombre; }
    public void setMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; }
    public String getMascotaEspecie() { return mascotaEspecie; }
    public void setMascotaEspecie(String mascotaEspecie) { this.mascotaEspecie = mascotaEspecie; }
    public String getMascotaRaza() { return mascotaRaza; }
    public void setMascotaRaza(String mascotaRaza) { this.mascotaRaza = mascotaRaza; }
    public String getMascotaColor() { return mascotaColor; }
    public void setMascotaColor(String mascotaColor) { this.mascotaColor = mascotaColor; }
    public String getMascotaSenas() { return mascotaSenas; }
    public void setMascotaSenas(String mascotaSenas) { this.mascotaSenas = mascotaSenas; }
    public String getDescripcionMascota() { return descripcionMascota; }
    public void setDescripcionMascota(String descripcionMascota) { this.descripcionMascota = descripcionMascota; }
    public List<AvistamientoEntity> getAvistamientos() { return avistamientos; }
}
