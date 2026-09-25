package petfinder.application.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import petfinder.application.port.entrada.ProcesadorComandosVoz.AccionVoz;
import petfinder.application.port.salida.ExtractorDatosReporte;
import petfinder.application.service.InterpreteComandoVoz.Comando;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Extractor de respaldo del asistente: entiende lo evidente de una frase con
 * expresiones regulares, sin red y sin costo.
 *
 * Existe por dos razones. La primera es que la app siga funcionando sin
 * clave de Anthropic, cuando Claude falla o cuando se agota el tope diario.
 * La segunda es demostrar la modificabilidad de la arquitectura: es el
 * segundo adaptador del mismo puerto, y el servicio no sabe cuál usa.
 *
 * Vive en application y no en un adaptador porque es lógica pura, sin
 * infraestructura. Reutiliza InterpreteComandoVoz para las frases completas
 * ("perdí un perro llamado Max en Chía") y agrega lo que ese intérprete no
 * mira: el contacto dicho en cualquier parte de la frase y las palabras
 * sueltas que revelan el tipo y la especie.
 */
public class ExtractorRegex implements ExtractorDatosReporte {

    /**
     * Celular colombiano: 3 y nueve dígitos, con espacios, puntos o guiones, y
     * con +57 opcional. Los lookarounds evitan tomar un pedazo de un número más
     * largo, como un documento de identidad.
     */
    private static final Pattern CELULAR = Pattern.compile(
            "(?<!\\d)(?:\\+?57[\\s-]?)?(3\\d{2})[\\s.-]?(\\d{3})[\\s.-]?(\\d{4})(?!\\d)");

    private static final Pattern CORREO = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");

    /**
     * "…en Chía mi número es…": a partir de aquí la frase habla del contacto y
     * no del lugar, así que se descarta antes de buscar la zona.
     */
    private static final Pattern ANUNCIO_CONTACTO = Pattern.compile(
            "(?iu)(?<!\\p{L})mi\\s+(?:n[uú]mero|celular|tel[eé]fono|correo|email|whatsapp)(?!\\p{L}).*");

    /**
     * Se toma del texto original, no del normalizado, para conservar tildes y
     * la ñ. Cada coincidencia se detiene antes del siguiente "en": si se
     * comiera el resto de la frase, nunca se llegaría al último lugar.
     */
    private static final Pattern ZONA = Pattern.compile(
            "(?<!\\p{L})[Ee][Nn]\\s+((?:(?!\\s+[Ee][Nn]\\s)[^,.;])+)");

    private static final Pattern NOMBRE_DICHO = Pattern.compile(
            "(?iu)(?<!\\p{L})(?:llamad[oa]|se\\s+llama)\\s+(\\p{L}+)");
    private static final Pattern NOMBRE_TRAS_ESPECIE = Pattern.compile(
            "(?<!\\p{L})(?:[Pp]err|[Gg]at)\\p{L}*\\s+(\\p{Lu}\\p{L}+)");

    private static final Pattern PERRO = Pattern.compile("(?<![a-z])perr[a-z]*");
    private static final Pattern GATO = Pattern.compile("(?<![a-z])gat[a-z]*");

    private final InterpreteComandoVoz interprete;

    public ExtractorRegex(InterpreteComandoVoz interprete) {
        this.interprete = interprete;
    }

    @Override
    public String nombre() {
        return "regex";
    }

    @Override
    public Optional<BorradorReporte> extraer(String texto, BorradorReporte contexto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        String contactoMedio = null;
        String sinContacto = texto;
        Matcher celular = CELULAR.matcher(texto);
        if (celular.find()) {
            contactoMedio = celular.group(1) + celular.group(2) + celular.group(3);
            sinContacto = celular.replaceFirst(" ");
        } else {
            Matcher correo = CORREO.matcher(texto);
            if (correo.find()) {
                contactoMedio = correo.group();
                sinContacto = correo.replaceFirst(" ");
            }
        }
        String cuerpo = ANUNCIO_CONTACTO.matcher(sinContacto).replaceFirst("");
        Comando comando = interprete.interpretar(cuerpo.split("[,.;]", -1)[0]);
        String normalizado = normalizar(cuerpo);

        TipoReporte tipo = tipoDe(comando, normalizado);
        BorradorReporte extraido = new BorradorReporte(
                tipo,
                nombreDe(comando, cuerpo),
                especieDe(comando, normalizado),
                null, null, null,
                tipo == TipoReporte.ENCONTRADA ? comando.descripcion() : null,
                zonaDe(cuerpo),
                null, null, null, null, null,
                contactoMedio);
        return extraido.equals(BorradorReporte.vacio()) ? Optional.empty() : Optional.of(extraido);
    }

    /** Gana la frase completa; si no la hay, la primera de las dos raíces que aparezca. */
    private static TipoReporte tipoDe(Comando comando, String normalizado) {
        if (comando.accion() == AccionVoz.REGISTRAR_PERDIDA) {
            return TipoReporte.PERDIDA;
        }
        if (comando.accion() == AccionVoz.REGISTRAR_ENCONTRADA) {
            return TipoReporte.ENCONTRADA;
        }
        int perdida = normalizado.indexOf("perd");
        int encontrada = normalizado.indexOf("encontr");
        if (perdida < 0 && encontrada < 0) {
            return null;
        }
        return encontrada < 0 || (perdida >= 0 && perdida < encontrada)
                ? TipoReporte.PERDIDA
                : TipoReporte.ENCONTRADA;
    }

    private static String nombreDe(Comando comando, String cuerpo) {
        if (comando.nombre() != null) {
            return comando.nombre();
        }
        Matcher dicho = NOMBRE_DICHO.matcher(cuerpo);
        if (dicho.find()) {
            return mayuscula(dicho.group(1));
        }
        Matcher trasEspecie = NOMBRE_TRAS_ESPECIE.matcher(cuerpo);
        return trasEspecie.find() ? trasEspecie.group(1) : null;
    }

    /** Si la frase nombra a los dos animales, cuenta el que se menciona primero. */
    private static String especieDe(Comando comando, String normalizado) {
        if (comando.especie() != null) {
            return comando.especie();
        }
        Matcher perro = PERRO.matcher(normalizado);
        Matcher gato = GATO.matcher(normalizado);
        boolean hayPerro = perro.find();
        boolean hayGato = gato.find();
        if (hayPerro && (!hayGato || perro.start() < gato.start())) {
            return "perro";
        }
        return hayGato ? "gato" : null;
    }

    /** Con varios "en" (tengo en mi casa… en el portal) el lugar es el último. */
    private static String zonaDe(String cuerpo) {
        Matcher matcher = ZONA.matcher(cuerpo);
        String zona = null;
        while (matcher.find()) {
            zona = matcher.group(1).strip();
        }
        return zona == null || zona.isEmpty() ? null : mayuscula(zona);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static String mayuscula(String texto) {
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
