package petfinder.adaptadores.entrada.web;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agrega las cabeceras de seguridad a todas las respuestas, de la página y de
 * la API.
 *
 * La política de contenido (CSP) es la defensa principal: aunque alguien
 * lograra colar un script en un texto de un reporte, el navegador no lo
 * ejecutaría, porque solo acepta scripts de este mismo sitio y de cdnjs. Cada
 * dominio externo está porque algo de la interfaz lo necesita: cdnjs sirve
 * Leaflet, Google las fuentes, y CARTO y OpenStreetMap las teselas del mapa.
 * No hay {@code 'unsafe-inline'}: por eso el registro del service worker vive
 * en {@code js/registro-sw.js} y no dentro del HTML.
 *
 * El micrófono y la ubicación quedan permitidos solo para este sitio porque
 * la voz y "Cerca de ti" los usan. El Referrer-Policy no es el más estricto a
 * propósito: las teselas de OpenStreetMap exigen que se envíe el origen.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FiltroCabecerasSeguridad extends OncePerRequestFilter {

    static final String POLITICA_CONTENIDO = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://cdnjs.cloudflare.com",
            "style-src 'self' https://fonts.googleapis.com https://cdnjs.cloudflare.com",
            "font-src https://fonts.gstatic.com",
            "img-src 'self' data: https://*.basemaps.cartocdn.com https://tile.openstreetmap.org"
                    + " https://cdnjs.cloudflare.com",
            "connect-src 'self'",
            "worker-src 'self'",
            "manifest-src 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "object-src 'none'");

    static final String POLITICA_PERMISOS = "microphone=(self), geolocation=(self), camera=(), payment=(), usb=()";

    /** Un año. El navegador la ignora en http, así que no estorba en localhost. */
    static final String TRANSPORTE_ESTRICTO = "max-age=31536000";

    /**
     * La consola de H2, que solo existe en el perfil local, usa marcos y
     * scripts dentro del HTML: con esta política no abriría.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return peticion.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        respuesta.setHeader("Content-Security-Policy", POLITICA_CONTENIDO);
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        respuesta.setHeader("Strict-Transport-Security", TRANSPORTE_ESTRICTO);
        respuesta.setHeader("Permissions-Policy", POLITICA_PERMISOS);
        respuesta.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        cadena.doFilter(peticion, respuesta);
    }
}
