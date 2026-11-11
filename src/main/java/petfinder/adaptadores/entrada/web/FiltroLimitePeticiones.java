package petfinder.adaptadores.entrada.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Limita cuántas peticiones por minuto acepta la API, con una cubeta de fichas
 * (token bucket) por IP y por grupo de rutas, y una cubeta global para el
 * asistente.
 *
 * Cubre tres amenazas del modelo: publicaciones abusivas (T2), recolección del
 * detalle de los casos (T5) y agotar el presupuesto de Claude (T7). Medido
 * antes de este filtro: 3000 POST con concurrencia 30 al asistente dieron 3000
 * respuestas 200 y ningún 429.
 *
 * La IP sale de {@code CF-Connecting-IP}, porque Render está detrás de
 * Cloudflare y Cloudflare la escribe; sin esa cabecera, de la conexión. No se
 * usa {@code server.forward-headers-strategy} (T9): con eso, cualquiera
 * mandaría {@code X-Forwarded-For: 127.0.0.1} y cambiaría lo que ve la app.
 * Fuera de Cloudflare (Compose, local) la cabecera se puede falsificar; por
 * eso el asistente, que es lo que cuesta dinero, tiene además un tope global.
 *
 * No lleva {@code @Component}: lo registra {@code config/} con orden
 * {@code HIGHEST_PRECEDENCE}, antes que {@link FiltroTamanoCuerpo}, porque
 * rechazar no necesita leer el cuerpo.
 */
public class FiltroLimitePeticiones extends OncePerRequestFilter {

    static final String MENSAJE = "Demasiadas peticiones. Espera un minuto e intenta de nuevo.";

    /** Segundos que se le piden al cliente antes de reintentar: lo que tarda en llenarse una cubeta. */
    static final int REINTENTAR_EN_SEGUNDOS = 60;

    /**
     * Tope de cubetas en memoria. Sin él, miles de IP falsas llenarían la
     * memoria; las que llevan más de un minuto quietas se borran primero.
     */
    static final int MAXIMO_CUBETAS = 10_000;

    private static final long UN_MINUTO_NANOS = 60_000_000_000L;

    /** Límites por minuto. Llegan de variables de entorno a través de config. */
    public record Limites(int asistentePorIp, int asistenteGlobal, int vozPorIp, int reportesPorIp) {

        public Limites {
            if (asistentePorIp < 1 || asistenteGlobal < 1 || vozPorIp < 1 || reportesPorIp < 1) {
                throw new IllegalArgumentException("Cada límite de peticiones debe ser al menos 1 por minuto");
            }
        }
    }

    private enum Grupo { ASISTENTE, VOZ, REPORTES }

    private final Limites limites;
    private final LongSupplier relojNanos;
    private final Map<String, Cubeta> cubetas = new ConcurrentHashMap<>();
    private final Cubeta asistenteGlobal;

    public FiltroLimitePeticiones(Limites limites) {
        this(limites, System::nanoTime);
    }

    /** Con reloj propio, para que las pruebas avancen el tiempo sin esperar. */
    FiltroLimitePeticiones(Limites limites, LongSupplier relojNanos) {
        this.limites = limites;
        this.relojNanos = relojNanos;
        this.asistenteGlobal = new Cubeta(limites.asistenteGlobal(), relojNanos.getAsLong());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        Grupo grupo = grupoDe(peticion.getRequestURI());
        if (grupo == null) {
            cadena.doFilter(peticion, respuesta);
            return;
        }
        long ahora = relojNanos.getAsLong();
        boolean permitida = tomarPorIp(grupo, ipDe(peticion), ahora)
                && (grupo != Grupo.ASISTENTE || asistenteGlobal.tomar(ahora));
        if (!permitida) {
            rechazar(respuesta);
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    private static Grupo grupoDe(String ruta) {
        if (ruta.startsWith("/api/asistente")) {
            return Grupo.ASISTENTE;
        }
        if (ruta.startsWith("/api/voz")) {
            return Grupo.VOZ;
        }
        if (ruta.startsWith("/api/reportes")) {
            return Grupo.REPORTES;
        }
        return null;
    }

    private static String ipDe(HttpServletRequest peticion) {
        String deCloudflare = peticion.getHeader("CF-Connecting-IP");
        return deCloudflare == null || deCloudflare.isBlank() ? peticion.getRemoteAddr() : deCloudflare.strip();
    }

    private boolean tomarPorIp(Grupo grupo, String ip, long ahora) {
        String clave = grupo + "|" + ip;
        Cubeta cubeta = cubetas.get(clave);
        if (cubeta == null) {
            if (cubetas.size() >= MAXIMO_CUBETAS) {
                cubetas.values().removeIf(c -> c.quietaDesde(ahora) > UN_MINUTO_NANOS);
            }
            if (cubetas.size() >= MAXIMO_CUBETAS) {
                // Lleno de IP activas en el último minuto: se rechaza la nueva en vez de crecer sin tope.
                return false;
            }
            cubeta = cubetas.computeIfAbsent(clave, k -> new Cubeta(limiteDe(grupo), ahora));
        }
        return cubeta.tomar(ahora);
    }

    private int limiteDe(Grupo grupo) {
        return switch (grupo) {
            case ASISTENTE -> limites.asistentePorIp();
            case VOZ -> limites.vozPorIp();
            case REPORTES -> limites.reportesPorIp();
        };
    }

    /** Mismo cuerpo {@code {"error": ...}} que {@link ManejadorErrores}, para que {@code api.js} lo trate igual. */
    private static void rechazar(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        respuesta.setHeader("Retry-After", String.valueOf(REINTENTAR_EN_SEGUNDOS));
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getOutputStream().write(("{\"error\":\"" + MENSAJE + "\"}").getBytes(StandardCharsets.UTF_8));
    }

    int cubetasEnMemoria() {
        return cubetas.size();
    }

    /**
     * Empieza llena y se recarga en línea recta: el límite completo cada
     * minuto. Así una ráfaga corta de alguien legítimo pasa, y un abuso
     * sostenido queda en el límite por minuto.
     */
    private static final class Cubeta {

        private final int capacidad;
        private double fichas;
        private long ultimaRecarga;

        Cubeta(int capacidad, long ahora) {
            this.capacidad = capacidad;
            this.fichas = capacidad;
            this.ultimaRecarga = ahora;
        }

        synchronized boolean tomar(long ahora) {
            long transcurrido = Math.max(0, ahora - ultimaRecarga);
            fichas = Math.min(capacidad, fichas + (double) transcurrido * capacidad / UN_MINUTO_NANOS);
            ultimaRecarga = ahora;
            if (fichas < 1) {
                return false;
            }
            fichas -= 1;
            return true;
        }

        synchronized long quietaDesde(long ahora) {
            return ahora - ultimaRecarga;
        }
    }
}
