package petfinder.domain.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entrega identificadores consecutivos con el formato PF-001.
 *
 * Se mantiene fuera de las entidades y del repositorio: un reporte no debería
 * inventar su propio identificador, y el repositorio solo guarda lo que ya
 * viene formado. Los creadores lo reciben en el constructor y lo consultan al
 * armar cada reporte.
 *
 * Es una clase concreta y no una interfaz porque no hay una segunda forma de
 * generar identificadores en este alcance. Crear la abstraccion por si acaso
 * seria generalidad especulativa; la inversion de dependencias se aplica
 * donde si hay una frontera real de sustitucion, que es el almacenamiento.
 *
 * Límite conocido: el contador vive en el proceso. Con una sola instancia de
 * la app basta; dos instancias contra la misma base repetirían números.
 */
public class GeneradorIdReportes {

    private static final Pattern FORMATO = Pattern.compile("PF-(\\d{1,9})");

    /**
     * AtomicInteger y no int: con la API, dos peticiones simultáneas podían
     * leer el mismo valor con secuencia++ y recibir el mismo PF-00X, y la
     * segunda sobrescribía a la primera en el repositorio.
     */
    private final AtomicInteger secuencia;

    /** Base vacía: el primer identificador es PF-001. */
    public GeneradorIdReportes() {
        this(0);
    }

    /**
     * Arranca después del último número que ya está en la base. Con la base
     * persistente (PostgreSQL), empezar siempre en 0 hacía que el primer POST
     * tras un reinicio devolviera 201 con PF-001 y pisara el reporte guardado.
     */
    public GeneradorIdReportes(int ultimoNumeroUsado) {
        if (ultimoNumeroUsado < 0) {
            throw new IllegalArgumentException("El último número usado no puede ser negativo: " + ultimoNumeroUsado);
        }
        this.secuencia = new AtomicInteger(ultimoNumeroUsado);
    }

    public String siguiente() {
        return String.format("PF-%03d", secuencia.incrementAndGet());
    }

    /**
     * Número de un identificador PF-, o 0 si no tiene ese formato. Existe para
     * comparar como número: como texto, "PF-1000" queda antes que "PF-999" y
     * el máximo saldría mal.
     */
    public static int numeroDe(String id) {
        if (id == null) {
            return 0;
        }
        Matcher m = FORMATO.matcher(id);
        return m.matches() ? Integer.parseInt(m.group(1)) : 0;
    }
}
