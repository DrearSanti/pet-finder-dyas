package petfinder.adaptadores.entrada.web.dto;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Topes de longitud de los textos que llegan por la API.
 *
 * Son los tamaños de las columnas donde se guardan: 255 caracteres para los
 * campos cortos (el VARCHAR que JPA crea por omisión) y 2000 para las
 * descripciones. Sin este control, un campo más largo llegaba hasta la base y
 * volvía como un 500 sin explicación; aquí vuelve como un 400 que nombra el
 * campo que hay que corregir.
 *
 * Vive en el adaptador web y no en el dominio porque no es una regla del
 * negocio: es un límite de lo que la API acepta y de lo que cabe en la tabla.
 * Java cuenta un emoji como dos caracteres y la base como uno, así que el tope
 * de aquí es igual o más estricto que el de la columna, nunca más laxo.
 */
public final class LongitudCampos {

    /** Campos cortos: zona, referencia, contacto, nombre, especie, raza, color, señas. */
    public static final int CORTO = 255;

    /** Descripciones: del caso, de la mascota encontrada y de un avistamiento. */
    public static final int LARGO = 2000;

    private LongitudCampos() {
    }

    /**
     * Devuelve el valor si cabe. Si no, rechaza la petición con un mensaje
     * para la persona que dice qué campo es y cuánto admite.
     */
    public static String exigir(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new DatosInvalidosException(
                    "El campo " + campo + " admite hasta " + maximo + " caracteres");
        }
        return valor;
    }
}
