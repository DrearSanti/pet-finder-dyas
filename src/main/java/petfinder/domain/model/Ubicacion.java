package petfinder.domain.model;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Lugar del hecho: zona o barrio, una referencia cercana y, si la persona lo
 * marcó en el mapa, sus coordenadas.
 *
 * Las coordenadas son opcionales y van juntas: un reporte por voz puede no
 * tenerlas, y un punto con latitud pero sin longitud no existe.
 */
public record Ubicacion(String zonaOBarrio, String referencia, Double latitud, Double longitud) {

    public Ubicacion {
        if ((latitud == null) != (longitud == null)) {
            throw new DatosInvalidosException("La ubicación necesita latitud y longitud juntas, o ninguna");
        }
        if (latitud != null && (latitud < -90 || latitud > 90)) {
            throw new DatosInvalidosException("La latitud debe estar entre -90 y 90");
        }
        if (longitud != null && (longitud < -180 || longitud > 180)) {
            throw new DatosInvalidosException("La longitud debe estar entre -180 y 180");
        }
    }

    /** Ubicación solo con texto, como en el Corte 1. */
    public Ubicacion(String zonaOBarrio, String referencia) {
        this(zonaOBarrio, referencia, null, null);
    }

    public boolean tieneCoordenadas() {
        return latitud != null;
    }

    /**
     * Versión que se puede mostrar en público: coordenadas redondeadas a tres
     * decimales (unos 100 m), para no exponer la dirección exacta de nadie.
     */
    public Ubicacion aproximada() {
        if (!tieneCoordenadas()) {
            return this;
        }
        return new Ubicacion(zonaOBarrio, referencia, redondear(latitud), redondear(longitud));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 1000d) / 1000d;
    }
}
