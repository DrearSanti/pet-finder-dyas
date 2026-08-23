package petfinder.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.exception.ReporteNoEncontradoException;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;
import petfinder.domain.observer.EventoAvistamiento;
import petfinder.domain.observer.ObservadorAvistamiento;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.infrastructure.persistence.RepositorioReportesEnMemoria;

/**
 * Pruebas del registro de avistamientos y del patrón Observer.
 *
 * Los observadores concretos del proyecto imprimen por consola, lo que no es
 * verificable en una prueba automática. Por eso aquí se usa un observador de
 * prueba que solo cuenta lo que recibe: lo que se está comprobando es el
 * mecanismo de notificación, no el texto que imprime cada suscriptor.
 *
 * Poder sustituir los observadores reales por este doble sin tocar el
 * publicador ni el servicio es, en sí mismo, la evidencia de bajo
 * acoplamiento que se documenta en la sección de análisis técnico.
 */
class ServicioAvistamientosTest {

    private ServicioReportes servicioReportes;
    private ServicioAvistamientos servicioAvistamientos;
    private PublicadorAvistamientos publicador;
    private ObservadorDePrueba primerObservador;
    private ObservadorDePrueba segundoObservador;

    @BeforeEach
    void prepararServicios() {
        GeneradorIdReportes generadorId = new GeneradorIdReportes();
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generadorId));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generadorId));

        RepositorioReportesEnMemoria repositorio = new RepositorioReportesEnMemoria();
        publicador = new PublicadorAvistamientos();
        primerObservador = new ObservadorDePrueba();
        segundoObservador = new ObservadorDePrueba();
        publicador.suscribir(primerObservador);
        publicador.suscribir(segundoObservador);

        servicioReportes = new ServicioReportes(repositorio, creadores);
        servicioAvistamientos = new ServicioAvistamientos(repositorio, publicador);
    }

    @Test
    @DisplayName("Un avistamiento valido queda asociado al reporte de perdida")
    void avistamientoValidoQuedaAsociado() {
        ReportePerdida reporte = registrarPerdida();

        servicioAvistamientos.registrar(reporte.getId(), avistamiento("AV-001"));

        assertEquals(1, reporte.getAvistamientos().size());
        assertEquals("AV-001", reporte.getAvistamientos().get(0).id());
    }

    @Test
    @DisplayName("Todos los observadores suscritos reciben el evento")
    void todosLosObservadoresSonNotificados() {
        ReportePerdida reporte = registrarPerdida();

        servicioAvistamientos.registrar(reporte.getId(), avistamiento("AV-001"));

        assertEquals(1, primerObservador.cantidadRecibida());
        assertEquals(1, segundoObservador.cantidadRecibida());
    }

    @Test
    @DisplayName("El observador desuscrito deja de recibir los eventos siguientes")
    void observadorDesuscritoNoRecibeElSiguienteEvento() {
        ReportePerdida reporte = registrarPerdida();
        servicioAvistamientos.registrar(reporte.getId(), avistamiento("AV-001"));

        publicador.desuscribir(primerObservador);
        servicioAvistamientos.registrar(reporte.getId(), avistamiento("AV-002"));

        // El retirado se queda en el evento que ya habia recibido; el que
        // sigue suscrito acumula los dos. Comprobar ambos numeros distingue
        // una desuscripcion real de un publicador que dejo de notificar a todos.
        assertEquals(1, primerObservador.cantidadRecibida());
        assertEquals(2, segundoObservador.cantidadRecibida());
    }

    @Test
    @DisplayName("Un identificador inexistente produce ReporteNoEncontradoException")
    void avistamientoSobreReporteInexistenteFalla() {
        assertThrows(ReporteNoEncontradoException.class,
                () -> servicioAvistamientos.registrar("PF-999", avistamiento("AV-001")));
    }

    @Test
    @DisplayName("Un reporte que ya no esta activo rechaza el avistamiento y no notifica")
    void avistamientoSobreReporteNoActivoEsRechazado() {
        ReportePerdida reporte = registrarPerdida();
        servicioReportes.resolver(reporte.getId());

        assertThrows(OperacionNoPermitidaException.class,
                () -> servicioAvistamientos.registrar(reporte.getId(), avistamiento("AV-001")));

        // La parte que de verdad importa: la validacion corta el flujo antes
        // de publicar. Nadie recibe una alerta por una operacion que fallo.
        assertEquals(0, primerObservador.cantidadRecibida());
        assertEquals(0, segundoObservador.cantidadRecibida());
        assertEquals(0, reporte.getAvistamientos().size());
    }

    @Test
    @DisplayName("Un reporte de mascota encontrada no admite avistamientos")
    void avistamientoSobreReporteEncontradaEsRechazado() {
        ReporteMascota encontrada = servicioReportes.registrar(
                TipoReporte.ENCONTRADA,
                SolicitudReporte.paraEncontrada(
                        new Ubicacion("Bella Suiza", "Frente a la panaderia"),
                        "Estaba solo en el anden",
                        new Contacto("Andres Melo", "andres@correo.com"),
                        "Gato gris con collar azul"));

        assertThrows(OperacionNoPermitidaException.class,
                () -> servicioAvistamientos.registrar(encontrada.getId(), avistamiento("AV-001")));
    }

    private ReportePerdida registrarPerdida() {
        return (ReportePerdida) servicioReportes.registrar(
                TipoReporte.PERDIDA,
                SolicitudReporte.paraPerdida(
                        new Ubicacion("Cedritos", "Parque de la 140"),
                        "Se solto de la correa",
                        new Contacto("Camila Rojas", "3001234567"),
                        new Mascota("Luna", "Perro", "Criolla", "Blanca", "Mancha negra")));
    }

    private Avistamiento avistamiento(String id) {
        return new Avistamiento(
                id,
                LocalDateTime.now(),
                new Ubicacion("Cedritos", "Parque de la calle 147"),
                "La vi tomando agua cerca de las canchas",
                null);
    }

    /**
     * Observador de prueba: no imprime nada, solo registra lo que recibe para
     * poder afirmarlo. Implementa la misma interfaz que los observadores
     * reales, así que el publicador no distingue entre uno y otro.
     */
    private static class ObservadorDePrueba implements ObservadorAvistamiento {

        private final List<EventoAvistamiento> recibidos = new ArrayList<>();

        @Override
        public void actualizar(EventoAvistamiento evento) {
            recibidos.add(evento);
        }

        int cantidadRecibida() {
            return recibidos.size();
        }
    }
}