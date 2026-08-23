package petfinder.ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

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

/**
 * Menú interactivo de consola. Traduce lo que el usuario escribe en llamadas a
 * los casos de uso y presenta los resultados.
 *
 * No contiene reglas de negocio: no decide qué clase de reporte construir
 * (solo elige un TipoReporte), no valida los datos más allá de leerlos, y no
 * sabe cómo se almacenan los reportes. Toda validación real ocurre en el
 * dominio y llega aquí como DominioException.
 */
public class MenuConsola {

    private final ServicioReportes servicioReportes;
    private final ServicioAvistamientos servicioAvistamientos;
    private final Scanner entrada = new Scanner(System.in);

    /** Consecutivo local de avistamientos; los reportes los numera el dominio. */
    private int consecutivoAvistamiento = 0;

    public MenuConsola(ServicioReportes servicioReportes,
                       ServicioAvistamientos servicioAvistamientos) {
        this.servicioReportes = servicioReportes;
        this.servicioAvistamientos = servicioAvistamientos;
    }

    public void ejecutar() {
        boolean continuar = true;
        while (continuar) {
            mostrarOpciones();
            String opcion = leer("Opcion");
            System.out.println();
            try {
                continuar = procesar(opcion);
            } catch (DominioException e) {
                // Las excepciones de dominio son mensajes para el usuario, no
                // fallas del programa: se muestran y el menú sigue vivo.
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
        }
        System.out.println("Hasta luego.");
    }

    private boolean procesar(String opcion) {
        switch (opcion) {
            case "1" -> registrarPerdida();
            case "2" -> registrarEncontrada();
            case "3" -> listarActivos();
            case "4" -> consultarReporte();
            case "5" -> registrarAvistamiento();
            case "6" -> cambiarEstado(true);
            case "7" -> cambiarEstado(false);
            case "0" -> {
                return false;
            }
            default -> System.out.println("Opcion no reconocida.");
        }
        return true;
    }

    private void mostrarOpciones() {
        System.out.println();
        System.out.println("=".repeat(50));
        System.out.println("  PET FINDER");
        System.out.println("=".repeat(50));
        System.out.println("  1. Registrar mascota perdida");
        System.out.println("  2. Registrar mascota encontrada");
        System.out.println("  3. Listar reportes activos");
        System.out.println("  4. Consultar un reporte");
        System.out.println("  5. Registrar un avistamiento");
        System.out.println("  6. Marcar reporte como RESUELTO");
        System.out.println("  7. Cerrar reporte");
        System.out.println("  0. Salir");
        System.out.println("-".repeat(50));
    }

    private void registrarPerdida() {
        Ubicacion ubicacion = leerUbicacion();
        String descripcion = leer("Que paso");
        Contacto contacto = leerContacto("propietario");
        Mascota mascota = new Mascota(
                leer("Nombre de la mascota"),
                leer("Especie"),
                leer("Raza"),
                leer("Color"),
                leer("Senas particulares"));

        ReporteMascota reporte = servicioReportes.registrar(
                TipoReporte.PERDIDA,
                SolicitudReporte.paraPerdida(ubicacion, descripcion, contacto, mascota));
        System.out.println("Reporte creado: " + reporte.getId());
    }

    private void registrarEncontrada() {
        Ubicacion ubicacion = leerUbicacion();
        String descripcion = leer("Donde y como la encontro");
        Contacto contacto = leerContacto("reportante");
        String descripcionMascota = leer("Describa la mascota encontrada");

        ReporteMascota reporte = servicioReportes.registrar(
                TipoReporte.ENCONTRADA,
                SolicitudReporte.paraEncontrada(
                        ubicacion, descripcion, contacto, descripcionMascota));
        System.out.println("Reporte creado: " + reporte.getId());
    }

    private void listarActivos() {
        List<ReporteMascota> activos = servicioReportes.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("No hay reportes activos.");
            return;
        }
        activos.forEach(r -> System.out.println("  " + r.resumen()));
    }

    private void consultarReporte() {
        ReporteMascota reporte = servicioReportes.consultar(leer("Identificador (PF-00X)"));
        System.out.println("  " + reporte.resumen());
        System.out.println("  Registrado el " + reporte.getFechaCreacion());
        System.out.println("  Detalle: " + reporte.getDescripcion());
    }

    private void registrarAvistamiento() {
        String idReporte = leer("Identificador del reporte de perdida");
        Ubicacion ubicacion = leerUbicacion();
        String descripcion = leer("Que vio");

        Contacto contacto = null;
        if (leer("Desea dejar sus datos de contacto? (s/n)").equalsIgnoreCase("s")) {
            contacto = leerContacto("testigo");
        }

        consecutivoAvistamiento++;
        servicioAvistamientos.registrar(idReporte, new Avistamiento(
                String.format("AV-%03d", consecutivoAvistamiento),
                LocalDateTime.now(), ubicacion, descripcion, contacto));
        System.out.println("Avistamiento registrado.");
    }

    private void cambiarEstado(boolean resolver) {
        String id = leer("Identificador del reporte");
        if (resolver) {
            servicioReportes.resolver(id);
            System.out.println("Reporte " + id + " marcado como RESUELTO.");
        } else {
            servicioReportes.cerrar(id);
            System.out.println("Reporte " + id + " CERRADO.");
        }
    }

    private Ubicacion leerUbicacion() {
        return new Ubicacion(leer("Zona o barrio"), leer("Punto de referencia"));
    }

    private Contacto leerContacto(String rol) {
        return new Contacto(
                leer("Nombre del " + rol),
                leer("Telefono o correo del " + rol));
    }

    private String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return entrada.nextLine().trim();
    }
}