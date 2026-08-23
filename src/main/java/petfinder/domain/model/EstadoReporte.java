package petfinder.domain.model;

/**
 * Estados posibles de un reporte y las transiciones válidas entre ellos.
 *
 * Las reglas de transición viven aquí y no en los servicios: así existe un
 * único lugar donde preguntar si un cambio de estado está permitido, y si
 * mañana se agrega un estado nuevo no hay que buscar validaciones repartidas
 * por toda la aplicación.
 */
public enum EstadoReporte {

    ACTIVO,
    RESUELTO,
    CERRADO;

    /**
     * Solo un reporte activo puede resolverse o cerrarse. RESUELTO y CERRADO
     * son terminales en este corte; reabrir casos quedó fuera de alcance.
     */
    public boolean permiteTransicionA(EstadoReporte destino) {
        return this == ACTIVO && (destino == RESUELTO || destino == CERRADO);
    }
}