package petfinder.domain.model;

/**
 * Identifica cada variante de reporte que el sistema sabe crear.
 *
 * Es la llave con la que ServicioReportes ubica el creador correspondiente.
 * Gracias a esto la interfaz de usuario nunca nombra a ReportePerdida ni a
 * ReporteEncontrada: solo traduce la opción del menú a uno de estos valores.
 */
public enum TipoReporte {
    PERDIDA,
    ENCONTRADA
}