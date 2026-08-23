package petfinder.domain.model;

/**
 * Datos descriptivos de la mascota que se está buscando.
 *
 * Se modela como objeto propio y no como cinco atributos sueltos del reporte
 * porque esta información tiene significado por sí sola y siempre viaja junta.
 * Pasar cinco cadenas separadas entre métodos es el olor de código conocido
 * como obsesión por los primitivos.
 */
public record Mascota(
        String nombre,
        String especie,
        String raza,
        String color,
        String senasParticulares) {
}