package petfinder;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import petfinder.application.ServicioAvistamientos;
import petfinder.application.ServicioReportes;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.ui.EscenarioDemostracion;
import petfinder.ui.MenuConsola;

/**
 * Entrada de consola del Corte 1, conservada como un adaptador más.
 *
 * Ya no ensambla nada a mano: levanta la misma ConfiguracionPetFinder que usa
 * la web, pero sin servidor, y le pide las piezas. Así la consola y la API
 * comparten un solo composition root.
 *
 * Uso: ./mvnw -q compile exec:java  (demostración)
 *      ./mvnw -q compile exec:java -Dexec.args="--menu"  (menú interactivo)
 */
public class Main {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext contexto = new SpringApplicationBuilder(PetFinderApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off", "logging.level.root=WARN")
                .run(args)) {

            ServicioReportes servicioReportes = contexto.getBean(ServicioReportes.class);
            ServicioAvistamientos servicioAvistamientos = contexto.getBean(ServicioAvistamientos.class);

            if (tieneBandera(args, "--menu")) {
                new MenuConsola(servicioReportes, servicioAvistamientos).ejecutar();
            } else {
                new EscenarioDemostracion(
                        servicioReportes, servicioAvistamientos,
                        contexto.getBean(PublicadorAvistamientos.class),
                        contexto.getBean(AlertaPropietarioObserver.class),
                        contexto.getBean(AuditoriaObserver.class)).ejecutar();
            }
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