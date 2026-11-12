package petfinder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import petfinder.adaptadores.entrada.web.FiltroLimitePeticiones;
import petfinder.adaptadores.entrada.web.FiltroLimitePeticiones.Limites;

/**
 * Registra el límite de peticiones (S10) con sus valores por minuto.
 *
 * Se registra aquí con un orden explícito, y no con {@code @Component}, para
 * que corra antes que FiltroTamanoCuerpo: con el mismo orden en los dos,
 * Spring no garantiza cuál va primero, y rechazar por exceso no necesita
 * leer el cuerpo.
 *
 * Los límites de voz y reportes quedan altos a propósito: la prueba corta del
 * despliegue manda unas 2000 peticiones por minuto desde una sola IP. El del
 * asistente es bajo porque cada llamada puede costar dinero en Claude.
 */
@Configuration
public class ConfiguracionLimites {

    @Bean
    public FilterRegistrationBean<FiltroLimitePeticiones> filtroLimitePeticiones(
            @Value("${petfinder.limite.asistente.por-ip}") int asistentePorIp,
            @Value("${petfinder.limite.asistente.global}") int asistenteGlobal,
            @Value("${petfinder.limite.voz.por-ip}") int vozPorIp,
            @Value("${petfinder.limite.reportes.por-ip}") int reportesPorIp) {
        FilterRegistrationBean<FiltroLimitePeticiones> registro = new FilterRegistrationBean<>(
                new FiltroLimitePeticiones(new Limites(asistentePorIp, asistenteGlobal, vozPorIp, reportesPorIp)));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registro.addUrlPatterns("/api/*");
        return registro;
    }
}
