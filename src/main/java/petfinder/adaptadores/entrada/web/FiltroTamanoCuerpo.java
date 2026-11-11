package petfinder.adaptadores.entrada.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rechaza con 413 cualquier cuerpo de más de 64 KB, antes de que llegue a
 * Jackson o a un controlador.
 *
 * El JSON más grande que manda la página, una tarjeta completa del asistente,
 * pesa pocos KB. Sin este tope, cuatro peticiones de 100 MB en paralelo
 * agotaron la memoria de un servidor de 384 MB: Jackson arma el texto entero
 * en memoria antes de que cualquier validación alcance a mirarlo.
 *
 * Si la petición declara su tamaño, basta con leer Content-Length. Si llega
 * por partes (chunked), se leen como máximo 64 KB más un byte: si sobra algo
 * se rechaza; si no, el controlador recibe el mismo cuerpo ya leído. Así el
 * tope no depende de que el cliente diga la verdad sobre el tamaño.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroTamanoCuerpo extends OncePerRequestFilter {

    /** 64 KB: más de diez veces la tarjeta más grande, y nada que la memoria note. */
    public static final int TAMANO_MAXIMO = 64 * 1024;

    static final String MENSAJE = "La petición es demasiado grande";

    private static final Set<String> METODOS_CON_CUERPO = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        long declarado = peticion.getContentLengthLong();
        if (declarado > TAMANO_MAXIMO) {
            rechazar(respuesta);
            return;
        }
        if (declarado < 0 && METODOS_CON_CUERPO.contains(peticion.getMethod())) {
            byte[] cuerpo = peticion.getInputStream().readNBytes(TAMANO_MAXIMO + 1);
            if (cuerpo.length > TAMANO_MAXIMO) {
                rechazar(respuesta);
                return;
            }
            cadena.doFilter(new PeticionYaLeida(peticion, cuerpo), respuesta);
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    /** Mismo cuerpo {@code {"error": ...}} que {@link ManejadorErrores}, para que {@code api.js} lo trate igual. */
    private static void rechazar(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpStatus.CONTENT_TOO_LARGE.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getOutputStream().write(
                ("{\"error\":\"" + MENSAJE + "\"}").getBytes(StandardCharsets.UTF_8));
    }

    /** La petición original, pero con el cuerpo que este filtro ya leyó y midió. */
    private static final class PeticionYaLeida extends HttpServletRequestWrapper {

        private final byte[] cuerpo;

        PeticionYaLeida(HttpServletRequest original, byte[] cuerpo) {
            super(original);
            this.cuerpo = cuerpo;
        }

        @Override
        public int getContentLength() {
            return cuerpo.length;
        }

        @Override
        public long getContentLengthLong() {
            return cuerpo.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream datos = new ByteArrayInputStream(cuerpo);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return datos.read();
                }

                @Override
                public int read(byte[] destino, int desde, int cuantos) {
                    return datos.read(destino, desde, cuantos);
                }

                @Override
                public boolean isFinished() {
                    return datos.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener oyente) {
                    try {
                        oyente.onDataAvailable();
                        oyente.onAllDataRead();
                    } catch (IOException e) {
                        oyente.onError(e);
                    }
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            String declarada = getCharacterEncoding();
            Charset codificacion = declarada == null ? StandardCharsets.UTF_8 : Charset.forName(declarada);
            return new BufferedReader(new InputStreamReader(getInputStream(), codificacion));
        }
    }
}
