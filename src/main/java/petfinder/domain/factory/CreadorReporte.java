package petfinder.domain.factory;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;

/**
 * Creador base del patrón Factory Method.
 *
 * Define el algoritmo completo de alta de un reporte —generar identificador,
 * validar lo común, construir— y deja a las subclases únicamente el paso de
 * construcción. Ese reparto es lo que distingue este patrón de una fábrica
 * simple: aquí no hay un switch que elija la clase, hay una subclase por
 * variante que sobrescribe el método fábrica.
 *
 * preparar() es final a propósito: el orden de los pasos es parte del contrato
 * y una subclase que lo alterara podría construir un reporte sin validar.
 */
public abstract class CreadorReporte {

    private final GeneradorIdReportes generador;

    protected CreadorReporte(GeneradorIdReportes generador) {
        this.generador = generador;
    }

    public final ReporteMascota preparar(SolicitudReporte solicitud) {
        validarDatosComunes(solicitud);
        String id = generador.siguiente();
        return crearReporte(solicitud, id);
    }

    /**
     * El método fábrica. Cada subclase decide qué clase concreta instancia y
     * valida lo que solo aplica a su tipo de reporte.
     */
    protected abstract ReporteMascota crearReporte(SolicitudReporte solicitud, String id);

    /**
     * Reglas que valen para cualquier reporte, sin importar el tipo. Está aquí
     * y no repetida en cada subclase para que exista un solo lugar donde
     * cambiarlas.
     */
    private void validarDatosComunes(SolicitudReporte solicitud) {
        if (solicitud == null) {
            throw new DatosInvalidosException("La solicitud no puede ser nula");
        }
        if (solicitud.ubicacion() == null
                || esVacio(solicitud.ubicacion().zonaOBarrio())) {
            throw new DatosInvalidosException(
                    "Se requiere la zona o barrio donde ocurrió el hecho");
        }
        if (esVacio(solicitud.descripcion())) {
            throw new DatosInvalidosException(
                    "Se requiere una descripción de lo ocurrido");
        }
        if (solicitud.contacto() == null
                || esVacio(solicitud.contacto().medioContacto())) {
            throw new DatosInvalidosException(
                    "Se requiere un medio de contacto para poder responder al reporte");
        }
    }

    /** Protegido para que las subclases validen sus campos con el mismo criterio. */
    protected static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}