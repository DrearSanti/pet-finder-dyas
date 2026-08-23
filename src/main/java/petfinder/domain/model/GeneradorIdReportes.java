package petfinder.domain.model;

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
 */
public class GeneradorIdReportes {

    private int secuencia = 0;

    public String siguiente() {
        secuencia++;
        return String.format("PF-%03d", secuencia);
    }
}