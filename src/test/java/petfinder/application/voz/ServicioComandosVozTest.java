package petfinder.application.voz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;
import petfinder.application.port.entrada.ProcesadorComandosVoz.ResultadoComandoVoz;
import petfinder.application.service.InterpreteComandoVoz;
import petfinder.application.service.ServicioComandosVoz;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * El servicio de voz se prueba contra un doble de GestionReportes: lo que se
 * verifica es qué le pide al caso de uso, no cómo se guarda. El intérprete es
 * el real (@Spy) porque es Java puro y no necesita doble.
 */
@ExtendWith(MockitoExtension.class)
class ServicioComandosVozTest {

    private static final Contacto CAMILA = new Contacto("Camila", "3001234567");

    @Mock
    private GestionReportes gestionReportes;

    @Spy
    private InterpreteComandoVoz interprete = new InterpreteComandoVoz();

    @InjectMocks
    private ServicioComandosVoz servicio;

    @Test
    @DisplayName("Una pérdida dictada llega a GestionReportes con el nombre, la especie y la zona dichos")
    void perdidaLlegaAlCasoDeUso() {
        // Arrange
        ReportePerdida creado = new ReportePerdida("PF-001", new Ubicacion("Chía", "Reportado por voz"),
                "perdí un perro llamado Max en Chía", new Mascota("Max", "perro", "", "", ""), CAMILA);
        when(gestionReportes.registrar(eq(TipoReporte.PERDIDA), any())).thenReturn(creado);
        ArgumentCaptor<SolicitudReporte> solicitud = ArgumentCaptor.forClass(SolicitudReporte.class);
        // Act
        ResultadoComandoVoz resultado = servicio.procesar("perdí un perro llamado Max en Chía", CAMILA);
        // Assert
        verify(gestionReportes).registrar(eq(TipoReporte.PERDIDA), solicitud.capture());
        assertEquals("Max", solicitud.getValue().mascota().nombre());
        assertEquals("perro", solicitud.getValue().mascota().especie());
        assertEquals("Chía", solicitud.getValue().ubicacion().zonaOBarrio());
        assertEquals("Reportado por voz", solicitud.getValue().ubicacion().referencia());
        assertEquals("perdí un perro llamado Max en Chía", solicitud.getValue().descripcion());
        assertEquals(CAMILA, solicitud.getValue().contacto());
        assertEquals(AccionVoz.REGISTRAR_PERDIDA, resultado.accion());
        assertEquals(List.of(creado), resultado.reportes());
    }

    @Test
    @DisplayName("Un hallazgo dictado llega a GestionReportes con la descripción del animal")
    void encontradaLlegaAlCasoDeUso() {
        // Arrange
        ReporteEncontrada creado = new ReporteEncontrada("PF-002", new Ubicacion("Cajicá", "Reportado por voz"),
                "encontré una gata blanca en Cajicá", "gata blanca", CAMILA);
        when(gestionReportes.registrar(eq(TipoReporte.ENCONTRADA), any())).thenReturn(creado);
        ArgumentCaptor<SolicitudReporte> solicitud = ArgumentCaptor.forClass(SolicitudReporte.class);
        // Act
        ResultadoComandoVoz resultado = servicio.procesar("encontré una gata blanca en Cajicá", CAMILA);
        // Assert
        verify(gestionReportes).registrar(eq(TipoReporte.ENCONTRADA), solicitud.capture());
        assertEquals("gata blanca", solicitud.getValue().descripcionMascota());
        assertEquals("Cajicá", solicitud.getValue().ubicacion().zonaOBarrio());
        assertEquals(AccionVoz.REGISTRAR_ENCONTRADA, resultado.accion());
    }

    @Test
    @DisplayName("Una frase no reconocida no registra nada y sugiere cómo decirla")
    void noReconocidaNoRegistra() {
        // Act
        ResultadoComandoVoz resultado = servicio.procesar("cómprame un perro", CAMILA);
        // Assert
        verify(gestionReportes, never()).registrar(any(), any());
        verifyNoInteractions(gestionReportes);
        assertEquals(AccionVoz.NO_RECONOCIDO, resultado.accion());
        assertEquals("No entendí. Prueba con: perdí un perro llamado Max en Chía", resultado.mensaje());
        assertTrue(resultado.reportes().isEmpty());
    }

    @Test
    @DisplayName("Ver reportes devuelve exactamente lo que respondió listarActivos")
    void listarDevuelveLoDelCasoDeUso() {
        // Arrange
        List<ReporteMascota> activos = List.of(new ReporteEncontrada("PF-003", new Ubicacion("Suba", "Portal"),
                "Estaba sola", "Perro café", CAMILA));
        when(gestionReportes.listarActivos()).thenReturn(activos);
        // Act
        ResultadoComandoVoz resultado = servicio.procesar("ver reportes", CAMILA);
        // Assert
        assertEquals(AccionVoz.LISTAR_ACTIVOS, resultado.accion());
        assertSame(activos, resultado.reportes());
        assertEquals("Hay 1 reporte activo.", resultado.mensaje());
    }

    @Test
    @DisplayName("Consultar un reporte por voz pide ese identificador al caso de uso")
    void consultarPideElIdentificador() {
        // Arrange
        ReporteEncontrada existente = new ReporteEncontrada("PF-001", new Ubicacion("Chía", "Parque"),
                "Estaba sola", "Gata gris", CAMILA);
        when(gestionReportes.consultar("PF-001")).thenReturn(existente);
        // Act
        ResultadoComandoVoz resultado = servicio.procesar("consultar reporte pf 1", CAMILA);
        // Assert
        assertEquals(AccionVoz.CONSULTAR, resultado.accion());
        assertEquals(List.of(existente), resultado.reportes());
    }
}
