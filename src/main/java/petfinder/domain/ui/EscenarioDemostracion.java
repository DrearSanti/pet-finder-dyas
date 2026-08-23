package petfinder.ui;

import java.time.LocalDateTime;

import petfinder.application.ServicioAvistamientos;
import petfinder.application.ServicioReportes;
import petfinder.domain.exception.DominioException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;

/**
 * Recorrido automático que demuestra el módulo completo en una sola corrida:
 * creación polimórfica, estados, validación, almacenamiento abstraído,
 * notificación a varios observadores y rechazo de operaciones inválidas.
 *
 * Se usa como guion de la sustentación. La salida está formateada con
 * separadores y encabezados para que se lea desde el fondo del salón.
 */
public class EscenarioDemostracion {

    private final ServicioReportes servicioReportes;
    private final ServicioAvistamientos servicioAvistamientos;
    private final PublicadorAvistamientos publicador;
    private final AlertaPropietarioObserver alerta;
    private final AuditoriaObserver auditoria;

    public EscenarioDemostracion(ServicioReportes servicioReportes,
                                 ServicioAvistamientos servicioAvistamientos,
                                 PublicadorAvistamientos publicador,
                                 AlertaPropietarioObserver alerta,
                                 AuditoriaObserver auditoria) {
        this.servicioReportes = servicioReportes;
        this.servicioAvistamientos = servicioAvistamientos;
        this.publicador = publicador;
        this.alerta = alerta;
        this.auditoria = auditoria;
    }

    public void ejecutar() {
        titulo("PET FINDER - DEMOSTRACION DEL MODULO");

        paso(1, "Observadores suscritos");
        System.out.println("  - AlertaPropietarioObserver");
        System.out.println("  - AuditoriaObserver");

        paso(2, "Se registra la perdida de Luna");
        ReporteMascota perdida = servicioReportes.registrar(
                TipoReporte.PERDIDA,
                SolicitudReporte.paraPerdida(
                        new Ubicacion("Cedritos", "Cerca del parque de la 140"),
                        "Se solto de la correa durante el paseo de la tarde",
                        new Contacto("Camila Rojas", "3001234567"),
                        new Mascota("Luna", "Perro", "Criolla", "Blanca",
                                "Mancha negra en el lomo")));
        System.out.println("  Creado: " + perdida.getId());
        System.out.println("  " + perdida.resumen());

        paso(3, "Se registra una mascota encontrada");
        ReporteMascota encontrada = servicioReportes.registrar(
                TipoReporte.ENCONTRADA,
                SolicitudReporte.paraEncontrada(
                        new Ubicacion("Bella Suiza", "Frente a la panaderia"),
                        "Estaba solo en el anden, se ve bien cuidado",
                        new Contacto("Andres Melo", "andres@correo.com"),
                        "Gato gris con collar azul sin placa"));
        System.out.println("  Creado: " + encontrada.getId());
        System.out.println("  " + encontrada.resumen());

        paso(4, "Reportes activos");
        listarActivos();

        paso(5, "Un ciudadano registra un avistamiento sobre " + perdida.getId());
        servicioAvistamientos.registrar(perdida.getId(), new Avistamiento(
                "AV-001",
                LocalDateTime.now(),
                new Ubicacion("Cedritos", "Parque de la calle 147"),
                "La vi tomando agua cerca de las canchas, salio corriendo",
                new Contacto("Vecino del sector", "3109876543")));

        paso(6, "Registro de auditoria acumulado");
        auditoria.getRegistro().forEach(linea -> System.out.println("  " + linea));

        paso(7, "Se desuscribe la alerta al propietario");
        publicador.desuscribir(alerta);
        System.out.println("  Solo queda suscrito AuditoriaObserver.");
        System.out.println("  Segundo avistamiento sobre " + perdida.getId() + ":");
        servicioAvistamientos.registrar(perdida.getId(), new Avistamiento(
                "AV-002",
                LocalDateTime.now(),
                new Ubicacion("Cedritos", "Conjunto residencial de la 145"),
                "Entro al parqueadero del conjunto",
                null));
        System.out.println("  (No aparecio alerta al propietario: el observador"
                + " retirado ya no recibe eventos.)");

        paso(8, "El propietario marca " + perdida.getId() + " como RESUELTO");
        servicioReportes.resolver(perdida.getId());
        System.out.println("  " + servicioReportes.consultar(perdida.getId()).resumen());

        paso(9, "Reportes activos despues de resolver");
        listarActivos();

        paso(10, "Se intenta agregar otro avistamiento a un caso ya resuelto");
        intentarFallido(() -> servicioAvistamientos.registrar(
                perdida.getId(), new Avistamiento(
                        "AV-003", LocalDateTime.now(),
                        new Ubicacion("Cedritos", "Calle 147"),
                        "Intento fuera de tiempo", null)));

        paso(11, "Se intenta consultar un reporte que no existe");
        intentarFallido(() -> servicioReportes.consultar("PF-999"));

        titulo("FIN DE LA DEMOSTRACION");
    }

    private void listarActivos() {
        var activos = servicioReportes.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("  (No hay reportes activos)");
            return;
        }
        activos.forEach(r -> System.out.println("  " + r.resumen()));
    }

    /**
     * Ejecuta una operación que debe fallar y muestra el mensaje de dominio.
     * Se captura DominioException y no Exception: si algo distinto explota, la
     * demostración debe caerse en vez de disimular un error real.
     */
    private void intentarFallido(Runnable operacion) {
        try {
            operacion.run();
            System.out.println("  ERROR: la operacion debio ser rechazada.");
        } catch (DominioException e) {
            System.out.println("  Rechazado correctamente: " + e.getMessage());
        }
    }

    private void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  " + texto);
        System.out.println("=".repeat(70));
    }

    private void paso(int numero, String descripcion) {
        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("PASO " + numero + ": " + descripcion);
        System.out.println("-".repeat(70));
    }
}