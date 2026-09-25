package petfinder.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import petfinder.application.port.entrada.AsistenteReportes;
import petfinder.application.port.salida.ExtractorDatosReporte;
import petfinder.application.service.ExtractorRegex;
import petfinder.application.service.InterpreteComandoVoz;
import petfinder.application.service.ServicioAsistente;

/**
 * Ensambla el asistente de la tarjeta viva. Vive aparte de
 * ConfiguracionPetFinder para que agregar un extractor nuevo no obligue a
 * tocar la configuración de los reportes.
 *
 * El orden decide cuál se prueba primero: menor número, antes. El de regex va
 * al final (100) como respaldo; el de Claude entrará con un número menor
 * (10) sin tocar este archivo ni el servicio.
 */
@Configuration
public class ConfiguracionAsistente {

    @Bean
    @Order(100)
    public ExtractorRegex extractorRegex(InterpreteComandoVoz interprete) {
        return new ExtractorRegex(interprete);
    }

    @Bean
    public AsistenteReportes asistenteReportes(List<ExtractorDatosReporte> extractores) {
        return new ServicioAsistente(extractores);
    }
}
