package petfinder.domain.model;

/**
 * Qué trae quien avisa sobre una mascota perdida.
 *
 * LA_VI es una pista: la vio pasar y sigue suelta. LA_TENGO es un hallazgo:
 * la persona la tiene bajo su cuidado. Se distinguen porque no pesan igual
 * para la familia: una pista ayuda a buscar, un hallazgo termina la búsqueda
 * y por eso exige un contacto y cambia cómo se muestra el caso.
 *
 * Es un avistamiento y no un reporte de mascota encontrada aparte porque
 * así queda pegado al caso de la mascota: la familia lo ve en su propio caso
 * en lugar de tener que encontrar otro reporte que hable de ella.
 */
public enum TipoAvistamiento {
    LA_VI,
    LA_TENGO
}
