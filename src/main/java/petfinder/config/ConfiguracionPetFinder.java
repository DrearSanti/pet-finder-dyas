package petfinder.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

/**
 * Composition root de Pet Finder: el único lugar donde se nombran las clases
 * concretas. Reemplaza el ensamblaje manual que hacía Main en el Corte 1.
 *
 * Los servicios y el dominio no llevan anotaciones de Spring: se construyen
 * aquí con new, y por eso siguen siendo Java puro y probables sin framework.
 */
@Configuration
public class ConfiguracionPetFinder {

    @Bean
    public GeneradorIdReportes generadorIdReportes() {
        return new GeneradorIdReportes();
    }

    @Bean
    public Map<TipoReporte, CreadorReporte> creadoresReporte(GeneradorIdReportes generador) {
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generador));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generador));
        return creadores;
    }

    @Bean
    public RepositorioReportes repositorioReportes() {
        return new RepositorioReportesEnMemoria();
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

    @Bean
    public ServicioReportes servicioReportes(RepositorioReportes repositorio,
                                             Map<TipoReporte, CreadorReporte> creadores) {
        return new ServicioReportes(repositorio, creadores);
    }

    @Bean
    public ServicioAvistamientos servicioAvistamientos(RepositorioReportes repositorio,
                                                       PublicadorAvistamientos publicador) {
        return new ServicioAvistamientos(repositorio, publicador);
    }
}