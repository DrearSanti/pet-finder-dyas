package petfinder.application.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;

/**
 * Entiende una frase dicha en voz alta y la convierte en un Comando. No
 * ejecuta nada: eso lo hace ServicioComandosVoz. Separarlos (SRP) permite
 * probar el reconocimiento sin repositorio ni mocks, y que el asistente
 * reutilice el mismo intérprete.
 *
 * Usa reglas y no un modelo de lenguaje a propósito: responde en
 * microsegundos, no cuesta nada, funciona sin red y siempre da la misma
 * respuesta para la misma frase, que es lo que exige una prueba de carga.
 */
public class InterpreteComandoVoz {

    /**
     * Límite de la frase. Un comando real cabe de sobra; algo más largo es un
     * dictado accidental o un abuso, y no vale la pena correr las expresiones
     * regulares sobre él.
     */
    public static final int LONGITUD_MAXIMA = 200;

    private static final Pattern PERDIDA = Pattern.compile(
            "^(?:se me )?perdi(?:o)? (?:a )?(?:mi |un |una )?(?<especie>[a-zñ]+) "
                    + "(?:llamad[oa]|que se llama) (?<nombre>[a-zñ]+) en (?<zona>.+)$");
    private static final Pattern ENCONTRADA = Pattern.compile(
            "^encontre (?:un |una )(?<descripcion>.+) en (?<zona>.+)$");
    private static final Pattern LISTAR = Pattern.compile(
            "^(?:ver|listar|mostrar)(?: los)? reportes(?: activos)?$");
    private static final Pattern CONSULTAR = Pattern.compile(
            "^consultar (?:el )?reporte pf[ -]?(?<numero>\\d{1,4})$");

    /**
     * Como el artículo de "perdí un perro" es opcional, en "perdí un llamado
     * Max" la expresión regular toma "un" como especie. Una especie que es un
     * artículo significa que la persona no dijo qué animal era.
     */
    private static final Set<String> NO_SON_ESPECIE = Set.of("a", "mi", "un", "una");

    /**
     * Lo que se entendió de la frase. Los campos que el comando no usa quedan
     * en null: una consulta solo trae idReporte, un listado no trae nada.
     */
    public record Comando(AccionVoz accion, String nombre, String especie, String zona,
                          String descripcion, String idReporte) {

        static Comando noReconocido() {
            return new Comando(AccionVoz.NO_RECONOCIDO, null, null, null, null, null);
        }
    }

    public Comando interpretar(String texto) {
        if (texto == null || texto.isBlank() || texto.length() > LONGITUD_MAXIMA) {
            return Comando.noReconocido();
        }
        String frase = normalizar(texto);

        Matcher perdida = PERDIDA.matcher(frase);
        if (perdida.matches() && !NO_SON_ESPECIE.contains(perdida.group("especie"))) {
            return new Comando(AccionVoz.REGISTRAR_PERDIDA,
                    capitalizar(perdida.group("nombre")),
                    normalizarEspecie(perdida.group("especie")),
                    capitalizar(perdida.group("zona")),
                    null, null);
        }
        Matcher encontrada = ENCONTRADA.matcher(frase);
        if (encontrada.matches()) {
            return new Comando(AccionVoz.REGISTRAR_ENCONTRADA, null, null,
                    capitalizar(encontrada.group("zona")),
                    encontrada.group("descripcion"), null);
        }
        if (LISTAR.matcher(frase).matches()) {
            return new Comando(AccionVoz.LISTAR_ACTIVOS, null, null, null, null, null);
        }
        Matcher consulta = CONSULTAR.matcher(frase);
        if (consulta.matches()) {
            String id = String.format("PF-%03d", Integer.parseInt(consulta.group("numero")));
            return new Comando(AccionVoz.CONSULTAR, null, null, null, null, id);
        }
        return Comando.noReconocido();
    }

    /**
     * El reconocedor del navegador escribe distinto la misma frase: con o sin
     * tildes, en mayúsculas, con signos. Normalizar antes de comparar hace que
     * las expresiones regulares solo tengan que conocer una forma. La ñ se
     * conserva porque cambia la palabra (año no es ano).
     */
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replace("ñ", "ñ")
                .replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("[.,¡!¿?]", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    /** "perrita", "perrito" y "perra" son el mismo animal para quien busca. */
    private static String normalizarEspecie(String especie) {
        if (especie.startsWith("perr")) {
            return "perro";
        }
        if (especie.startsWith("gat")) {
            return "gato";
        }
        return especie;
    }

    private static String capitalizar(String texto) {
        return Arrays.stream(texto.split(" "))
                .map(palabra -> palabra.substring(0, 1).toUpperCase(Locale.ROOT) + palabra.substring(1))
                .collect(Collectors.joining(" "));
    }
}
