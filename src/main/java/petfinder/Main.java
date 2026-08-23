package petfinder;

import java.util.EnumMap;
import java.util.Map;

import petfinder.application.ServicioAvistamientos;
import petfinder.application.ServicioReportes;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.domain.repository.RepositorioReportes;
import petfinder.infrastructure.persistence.RepositorioReportesEnMemoria;
import petfinder.ui.EscenarioDemostracion;
import petfinder.ui.MenuConsola;

/**
 * Punto de entrada y único lugar donde se nombran las implementaciones
 * concretas del sistema.
 *
 * Todo el resto de la aplicación trabaja contra abstracciones: los servicios
 * reciben RepositorioReportes y CreadorReporte, y el publicador conoce a sus
 * suscriptores solo como ObservadorAvistamiento. Concentrar aquí el
 * ensamblaje es lo que hace que esa independencia sea real y no solo
 * declarativa, y es la evidencia de DIP que se documenta en el README.
 */
public class Main {

    public static void main(String[] args) {
        // --- Creación: un solo generador compartido por ambos creadores, para
        // que los identificadores PF-001, PF-002... sean una secuencia única y
        // no se reinicien por tipo de reporte.
        GeneradorIdReportes generadorId = new GeneradorIdReportes();

        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generadorId));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generadorId));

        // --- Almacenamiento: la variable se declara con el tipo de la interfaz
        // a propósito. Cambiar a una base de datos sería cambiar esta línea y
        // ninguna otra del proyecto.
        RepositorioReportes repositorio = new RepositorioReportesEnMemoria();

        // --- Notificación: los observadores se suscriben aquí, no dentro del
        // servicio. ServicioAvistamientos nunca sabe cuántos son ni de qué tipo.
        PublicadorAvistamientos publicador = new PublicadorAvistamientos();
        AlertaPropietarioObserver alerta = new AlertaPropietarioObserver();
        AuditoriaObserver auditoria = new AuditoriaObserver();
        publicador.suscribir(alerta);
        publicador.suscribir(auditoria);

        // --- Casos de uso: reciben sus colaboradores ya construidos.
        ServicioReportes servicioReportes = new ServicioReportes(repositorio, creadores);
        ServicioAvistamientos servicioAvistamientos =
                new ServicioAvistamientos(repositorio, publicador);

        if (tieneBandera(args, "--menu")) {
            new MenuConsola(servicioReportes, servicioAvistamientos).ejecutar();
        } else {
            new EscenarioDemostracion(
                    servicioReportes, servicioAvistamientos,
                    publicador, alerta, auditoria).ejecutar();
        }
    }

    private static boolean tieneBandera(String[] args, String bandera) {
        for (String arg : args) {
            if (bandera.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}