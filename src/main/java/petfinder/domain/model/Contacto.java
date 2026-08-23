package petfinder.domain.model;

/**
 * Persona con la que hay que comunicarse por un reporte, junto con el medio
 * que dejó disponible. Lo usan tanto el propietario de una mascota perdida
 * como quien reporta haberla encontrado o avistado.
 */
public record Contacto(String nombre, String medioContacto) {
}