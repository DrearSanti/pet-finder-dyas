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
        Frase frase = Frase.de(texto);

        Matcher perdida = PERDIDA.matcher(frase.normalizada());
        if (perdida.matches() && !NO_SON_ESPECIE.contains(perdida.group("especie"))) {
            return new Comando(AccionVoz.REGISTRAR_PERDIDA,
                    capitalizar(frase.original(perdida, "nombre")),
                    normalizarEspecie(perdida.group("especie")),
                    capitalizar(frase.original(perdida, "zona")),
                    null, null);
        }
        Matcher encontrada = ENCONTRADA.matcher(frase.normalizada());
        if (encontrada.matches()) {
            return new Comando(AccionVoz.REGISTRAR_ENCONTRADA, null, null,
                    capitalizar(frase.original(encontrada, "zona")),
                    frase.original(encontrada, "descripcion"), null);
        }
        if (LISTAR.matcher(frase.normalizada()).matches()) {
            return new Comando(AccionVoz.LISTAR_ACTIVOS, null, null, null, null, null);
        }
        Matcher consulta = CONSULTAR.matcher(frase.normalizada());
        if (consulta.matches()) {
            String id = String.format("PF-%03d", Integer.parseInt(consulta.group("numero")));
            return new Comando(AccionVoz.CONSULTAR, null, null, null, null, id);
        }
        return Comando.noReconocido();
    }

    /**
     * La frase en dos versiones. El reconocedor del navegador escribe distinto
     * la misma frase: con o sin tildes, en mayúsculas, con signos. Las
     * expresiones regulares corren sobre la normalizada para conocer una sola
     * forma. Pero lo que se guarda (nombre, zona, descripción) se copia de la
     * original: quitar tildes sirve para entender, no para guardar, y "Chía"
     * tiene que llegar a la base igual que por el formulario o el asistente.
     *
     * @param posiciones posiciones[i] es el índice en original de la letra i de normalizada
     */
    private record Frase(String normalizada, String original, int[] posiciones) {

        private static final String SIGNOS = ".,¡!¿?";

        static Frase de(String texto) {
            // Compuesta: así una tilde escrita aparte de su letra no ocupa una posición propia.
            String original = Normalizer.normalize(texto, Normalizer.Form.NFC);
            StringBuilder normalizada = new StringBuilder();
            int[] posiciones = new int[original.length() * 3 + 1];
            for (int i = 0; i < original.length(); i++) {
                for (char letra : sinTildeNiSigno(original.charAt(i)).toCharArray()) {
                    boolean espacio = Character.isWhitespace(letra);
                    boolean sobra = espacio && (normalizada.isEmpty()
                            || normalizada.charAt(normalizada.length() - 1) == ' ');
                    if (!sobra) {
                        posiciones[normalizada.length()] = i;
                        normalizada.append(espacio ? ' ' : letra);
                    }
                }
            }
            if (!normalizada.isEmpty() && normalizada.charAt(normalizada.length() - 1) == ' ') {
                normalizada.setLength(normalizada.length() - 1);
            }
            return new Frase(normalizada.toString(), original, posiciones);
        }

        /** La ñ se conserva porque cambia la palabra (año no es ano). */
        private static String sinTildeNiSigno(char letra) {
            if (letra == 'ñ' || letra == 'Ñ') {
                return "ñ";
            }
            if (SIGNOS.indexOf(letra) >= 0) {
                return " ";
            }
            return Normalizer.normalize(String.valueOf(letra).toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
        }

        /**
         * El trozo de la frase original que corresponde a un grupo de la
         * normalizada, con sus tildes. Se pasa a minúsculas y se limpia de
         * signos igual que la normalizada, para que solo cambien las tildes.
         */
        String original(Matcher coincidencia, String grupo) {
            int inicio = coincidencia.start(grupo);
            int fin = coincidencia.end(grupo);
            return original.substring(posiciones[inicio], posiciones[fin - 1] + 1)
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[.,¡!¿?]", " ")
                    .replaceAll("\\s+", " ")
                    .strip();
        }
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
