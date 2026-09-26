package petfinder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import petfinder.application.port.salida.RepositorioReportes;
import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.Ubicacion;
import petfinder.domain.observer.EventoAvistamiento;
import petfinder.domain.observer.PublicadorAvistamientos;

/**
 * Pruebas de {@link ServicioAvistamientos} con dobles de Mockito.
 *
 * Las pruebas del Corte 1 usan el repositorio en memoria y un observador que
 * cuenta eventos: verifican el resultado. Estas verifican las interacciones
 * que ese resultado no deja ver: que una operación rechazada no llega a
 * guardar ni a notificar, y que el orden guardar → notificar se respeta,
 * porque avisarle al dueño de una pista que no quedó guardada sería mentirle.
 *
 * Solo se simulan los colaboradores (repositorio y publicador); los reportes
 * son objetos reales del dominio, para no probar un dominio inventado.
 */
@ExtendWith(MockitoExtension.class)
class ServicioAvistamientosMockTest {

    @Mock
    private RepositorioReportes repositorio;

    @Mock
    private PublicadorAvistamientos publicador;

    @InjectMocks
    private ServicioAvistamientos servicio;

    private static ReportePerdida perdida(String id, EstadoReporte estado) {
        return ReportePerdida.reconstruir(id, new Ubicacion("Cedritos", "Parque de la 147"),
                "Se escapó en la tarde", new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"), LocalDateTime.of(2026, 9, 24, 16, 30), estado, List.of());
    }

    private static Avistamiento avistamiento() {
        return new Avistamiento("AV-1a2b3c4d", LocalDateTime.of(2026, 9, 24, 17, 10),
                new Ubicacion("Cedritos", "Canchas"), "La vi corriendo", null);
    }

    @Test
    @DisplayName("Un avistamiento sobre un reporte inexistente lanza la excepción y no guarda ni notifica")
    void registrarEnReporteInexistenteNoGuardaNiNotifica() {
        // Arrange
        when(repositorio.buscarPorId("PF-404")).thenReturn(Optional.empty());

        // Act
        assertThrows(ReporteNoEncontradoException.class, () -> servicio.registrar("PF-404", avistamiento()));

        // Assert
        verify(repositorio, never()).guardar(any());
        verify(publicador, never()).notificar(any());
    }

    @Test
    @DisplayName("Un avistamiento sobre un reporte de mascota encontrada se rechaza y no guarda ni notifica")
    void registrarEnReporteDeEncontradaLanzaExcepcionYNoNotifica() {
        // Arrange
        ReporteEncontrada encontrada = ReporteEncontrada.reconstruir("PF-002", new Ubicacion("Chía", null),
                "Estaba en la puerta del supermercado", "Gata gris con collar rojo",
                new Contacto("Sofía", "sofia@correo.co"), LocalDateTime.of(2026, 9, 24, 9, 0), EstadoReporte.ACTIVO);
        when(repositorio.buscarPorId("PF-002")).thenReturn(Optional.of(encontrada));

        // Act
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.registrar("PF-002", avistamiento()));

        // Assert
        verify(repositorio, never()).guardar(any());
        verify(publicador, never()).notificar(any());
    }

    @Test
    @DisplayName("Un avistamiento sobre un reporte RESUELTO se rechaza y no guarda ni notifica")
    void registrarEnReporteResueltoLanzaExcepcionYNoNotifica() {
        // Arrange
        ReportePerdida resuelto = perdida("PF-003", EstadoReporte.RESUELTO);
        when(repositorio.buscarPorId("PF-003")).thenReturn(Optional.of(resuelto));

        // Act
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.registrar("PF-003", avistamiento()));

        // Assert
        assertTrue(resuelto.getAvistamientos().isEmpty());
        verify(repositorio, never()).guardar(any());
        verify(publicador, never()).notificar(any());
    }

    @Test
    @DisplayName("Un avistamiento válido se guarda antes de notificar, y el evento lleva el caso y la pista")
    void registrarValidoGuardaAntesDeNotificar() {
        // Arrange
        ReportePerdida activo = perdida("PF-004", EstadoReporte.ACTIVO);
        Avistamiento pista = avistamiento();
        when(repositorio.buscarPorId("PF-004")).thenReturn(Optional.of(activo));

        // Act
        servicio.registrar("PF-004", pista);

        // Assert
        InOrder orden = inOrder(repositorio, publicador);
        orden.verify(repositorio).guardar(activo);
        ArgumentCaptor<EventoAvistamiento> evento = ArgumentCaptor.forClass(EventoAvistamiento.class);
        orden.verify(publicador).notificar(evento.capture());
        orden.verifyNoMoreInteractions();
        assertEquals(List.of(pista), activo.getAvistamientos());
        assertEquals("PF-004", evento.getValue().idReporte());
        assertEquals("Luna", evento.getValue().nombreMascota());
        assertEquals(pista, evento.getValue().avistamiento());
    }
}
