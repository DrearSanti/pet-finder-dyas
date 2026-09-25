package petfinder.application.port.entrada;

import petfinder.domain.model.Avistamiento;

/**
 * Puerto de entrada para registrar una pista sobre una mascota perdida.
 * Lo implementa ServicioAvistamientos, que conserva las reglas y el Observer.
 */
public interface RegistroAvistamientos {

    void registrar(String idReporte, Avistamiento avistamiento);
}
