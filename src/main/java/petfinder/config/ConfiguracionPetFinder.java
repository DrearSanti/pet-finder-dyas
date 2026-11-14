package petfinder.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.entrada.ProcesadorComandosVoz;
import petfinder.application.service.InterpreteComandoVoz;
import petfinder.application.service.ServicioAvistamientos;
import petfinder.application.service.ServicioComandosVoz;
import petfinder.application.service.ServicioReportes;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.CodigoGestion;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.application.port.salida.RepositorioReportes;
import petfinder.adaptadores.salida.persistencia.h2.ReporteJpaRepository;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;

/**
 * Composition root de Pet Finder: el único lugar donde se nombran las clases
 * concretas. Reemplaza el ensamblaje manual que hacía Main en el Corte 1.
 *
 * Los servicios y el dominio no llevan anotaciones de Spring: se construyen
 * aquí con new, y por eso siguen siendo Java puro y probables sin framework.
 */
@Configuration
public class ConfiguracionPetFinder {

    /**
     * Arranca después del último PF- guardado: con PostgreSQL la base sobrevive
     * a los reinicios y el contador en memoria no.
     */
    @Bean
    public GeneradorIdReportes generadorIdReportes(RepositorioReportes repositorio) {
        return new GeneradorIdReportes(repositorio.ultimoNumeroDeId());
    }

    @Bean
    public Map<TipoReporte, CreadorReporte> creadoresReporte(GeneradorIdReportes generador) {
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generador));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generador));
        return creadores;
    }

    /**
     * La aplicación guarda en H2. RepositorioReportesEnMemoria sigue existiendo
     * para las pruebas unitarias: cambiar de adaptador es cambiar esta línea.
     * El tope de la lista (petfinder.lista.maximo) llega desde aquí para que
     * el puerto no cambie de firma.
     */
    @Bean
    public RepositorioReportes repositorioReportes(ReporteJpaRepository jpa,
            @Value("${petfinder.lista.maximo:" + RepositorioReportesH2.MAXIMO_POR_OMISION + "}") int maximoLista) {
        return new RepositorioReportesH2(jpa, maximoLista);
    }

    @Bean
    public AlertaPropietarioObserver alertaPropietarioObserver() {
        return new AlertaPropietarioObserver();
    }

    @Bean
    public AuditoriaObserver auditoriaObserver() {
        return new AuditoriaObserver();
    }

    @Bean
    public PublicadorAvistamientos publicadorAvistamientos(AlertaPropietarioObserver alerta,
                                                           AuditoriaObserver auditoria) {
        PublicadorAvistamientos publicador = new PublicadorAvistamientos();
        publicador.suscribir(alerta);
        publicador.suscribir(auditoria);
        return publicador;
    }

    /** Un código de administrador más corto que esto se adivina; la app no arranca con él. */
    static final int LARGO_MINIMO_CODIGO_ADMIN = 20;

    /**
     * El código de administrador (PETFINDER_CODIGO_ADMIN) resuelve o cierra
     * cualquier caso, incluidos los de ejemplo y los publicados antes del
     * código de gestión. Aquí se convierte en huella y el servicio nunca ve el
     * valor; tampoco se escribe en el log. Vacío significa "sin administrador".
     */
    @Bean
    public ServicioReportes servicioReportes(RepositorioReportes repositorio,
                                             Map<TipoReporte, CreadorReporte> creadores,
                                             @Value("${petfinder.codigo-admin:}") String codigoAdmin) {
        return new ServicioReportes(repositorio, creadores, huellaAdministrador(codigoAdmin));
    }

    static String huellaAdministrador(String codigoAdmin) {
        if (codigoAdmin == null || codigoAdmin.isBlank()) {
            return null;
        }
        if (codigoAdmin.strip().length() < LARGO_MINIMO_CODIGO_ADMIN) {
            throw new IllegalStateException("PETFINDER_CODIGO_ADMIN debe tener al menos "
                    + LARGO_MINIMO_CODIGO_ADMIN + " caracteres (genera uno con: openssl rand -base64 24)");
        }
        return CodigoGestion.huellaDe(codigoAdmin.strip());
    }

    @Bean
    public ServicioAvistamientos servicioAvistamientos(RepositorioReportes repositorio,
                                                       PublicadorAvistamientos publicador) {
        return new ServicioAvistamientos(repositorio, publicador);
    }

    @Bean
    public InterpreteComandoVoz interpreteComandoVoz() {
        return new InterpreteComandoVoz();
    }

    /**
     * Recibe el puerto GestionReportes y no ServicioReportes: la voz queda
     * atada al caso de uso, no a su implementación.
     */
    @Bean
    public ProcesadorComandosVoz procesadorComandosVoz(InterpreteComandoVoz interprete,
                                                      GestionReportes gestionReportes) {
        return new ServicioComandosVoz(interprete, gestionReportes);
    }
}