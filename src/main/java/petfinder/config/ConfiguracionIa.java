package petfinder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import petfinder.adaptadores.salida.ia.ClienteModeloAnthropic;
import petfinder.adaptadores.salida.ia.ExtractorClaude;

/**
 * Conecta el extractor de Claude al asistente. Es un extractor más: el orden
 * decide cuál se prueba primero, y este va antes (10) que el de regex (100).
 * Al servicio del asistente no se le toca.
 *
 * Sin clave la app arranca igual: el extractor queda inactivo, nunca llama a
 * Anthropic y el asistente responde con regex. La clave llega solo por
 * variable de entorno o .env; jamás se escribe ni se registra en el código.
 *
 * El modelo se lee aquí y se le pasa al cliente: cambiar de modelo es cambiar
 * una variable, no recompilar.
 */
@Configuration
public class ConfiguracionIa {

    @Bean
    @Order(10)
    public ExtractorClaude extractorClaude(
            @Value("${ANTHROPIC_API_KEY:}") String claveApi,
            @Value("${PETFINDER_IA_MODELO:claude-sonnet-5}") String modelo,
            @Value("${PETFINDER_IA_TOPE_DIARIO:200}") int topeDiario) {
        ExtractorClaude.ClienteModelo cliente = claveApi.isBlank()
                ? null
                : new ClienteModeloAnthropic(claveApi, modelo);
        return new ExtractorClaude(cliente, claveApi, topeDiario);
    }
}
