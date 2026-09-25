package petfinder.adaptadores.entrada.web;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import petfinder.adaptadores.entrada.web.dto.AvistamientoDTO;
import petfinder.adaptadores.entrada.web.dto.ReporteDTO;
import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.RegistroAvistamientos;

/**
 * "La vi": registra una pista sobre una mascota perdida.
 *
 * El id del avistamiento se genera aquí y no en el navegador, para que nadie
 * pueda elegirlo ni repetirlo, y lleva el prefijo {@code AV-} para distinguirlo
 * a simple vista de un reporte ({@code PF-}). Las reglas (solo pérdidas, solo
 * activas) son del puerto: si fallan, {@link ManejadorErrores} responde 404 o
 * 409 sin que este controlador lo sepa.
 */
@RestController
public class AvistamientoController {

    private final RegistroAvistamientos registroAvistamientos;
    private final GestionReportes gestionReportes;

    public AvistamientoController(RegistroAvistamientos registroAvistamientos,
                                  GestionReportes gestionReportes) {
        this.registroAvistamientos = registroAvistamientos;
        this.gestionReportes = gestionReportes;
    }

    /**
     * Responde con el caso ya actualizado para que la página muestre la pista
     * nueva sin pedirlo otra vez.
     */
    @PostMapping("/api/reportes/{id}/avistamientos")
    public ResponseEntity<ReporteDTO> registrar(@PathVariable String id, @RequestBody AvistamientoDTO cuerpo) {
        String idAvistamiento = "AV-" + UUID.randomUUID().toString().substring(0, 8);
        registroAvistamientos.registrar(id, cuerpo.aAvistamiento(idAvistamiento, LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReporteDTO.detalle(gestionReportes.consultar(id)));
    }
}
