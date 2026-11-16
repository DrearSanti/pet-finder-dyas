package petfinder.adaptadores.entrada.web;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrega al navegador la clave de CARTO para las teselas del mapa, leída al
 * arrancar desde la variable de entorno {@code PETFINDER_CLAVE_CARTO}.
 *
 * Antes la clave se escribía en {@code js/config-local.js} al construir la
 * imagen, así que quedaba dentro de una imagen que se publica en un registro
 * público y había que reconstruir para cambiarla. Leerla al arrancar deja una
 * sola imagen para todos los entornos y ningún valor de configuración adentro.
 * La clave sigue siendo visible en el navegador por diseño: viaja en cada
 * petición de teselas.
 *
 * Responde en la misma ruta que ya carga {@code index.html}, y con 200 y cuerpo
 * vacío si no hay clave, para que el mapa caiga a OpenStreetMap sin un error en
 * la consola. Solo se dejan letras, números, guion y guion bajo: el valor
 * termina dentro de un script y no puede cerrar la cadena.
 */
@RestController
public class ConfiguracionMapaController {

    private static final MediaType JAVASCRIPT = new MediaType("text", "javascript", StandardCharsets.UTF_8);

    private final String claveCarto;

    public ConfiguracionMapaController(@Value("${PETFINDER_CLAVE_CARTO:}") String claveCarto) {
        this.claveCarto = claveCarto.replaceAll("[^A-Za-z0-9_-]", "");
    }

    @GetMapping("/js/config-local.js")
    public ResponseEntity<String> configuracion() {
        String cuerpo = claveCarto.isEmpty() ? "" : "window.PETFINDER_CLAVE_CARTO = '" + claveCarto + "';\n";
        return ResponseEntity.ok().contentType(JAVASCRIPT).cacheControl(CacheControl.noCache()).body(cuerpo);
    }
}
