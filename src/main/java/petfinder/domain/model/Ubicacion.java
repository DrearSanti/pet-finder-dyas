package petfinder.domain.model;

/**
 * Lugar expresado en texto: zona o barrio y una referencia cercana.
 *
 * En este corte no hay coordenadas ni mapa. La estructura queda lista para que
 * una versión futura agregue latitud y longitud sin tocar a quienes la usan.
 */
public record Ubicacion(String zonaOBarrio, String referencia) {
}