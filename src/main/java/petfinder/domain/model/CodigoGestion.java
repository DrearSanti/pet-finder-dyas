package petfinder.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Código de gestión de un caso: lo recibe una sola vez quien publica, y es lo
 * único que le permite resolverlo o cerrarlo (amenaza T1). Antes cualquiera
 * podía cerrar el caso de otra familia sabiendo su número, que sale en el mapa.
 *
 * Se guarda solo su huella (SHA-256), nunca el código: si alguien leyera la
 * base, no podría cerrar casos con lo que encuentre ahí. 128 bits aleatorios
 * hacen inútil adivinarlo, así que una huella sin sal basta; no es una clave
 * que una persona elija.
 *
 * {@link #toString()} no lo muestra, para que nunca termine en un log por
 * accidente.
 */
public record CodigoGestion(String valor) {

    private static final SecureRandom AZAR = new SecureRandom();

    /** 16 bytes: 128 bits, que en Base64URL sin relleno son 22 caracteres. */
    private static final int BYTES = 16;

    public CodigoGestion {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El código de gestión no puede estar vacío");
        }
    }

    /** Un código nuevo, impredecible, que se puede copiar de una URL o un mensaje sin escaparlo. */
    public static CodigoGestion generar() {
        byte[] bytes = new byte[BYTES];
        AZAR.nextBytes(bytes);
        return new CodigoGestion(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    /** Lo que se guarda en la base en lugar del código. */
    public String huella() {
        return huellaDe(valor);
    }

    /** SHA-256 en hexadecimal (64 caracteres) de cualquier texto: la usa también el código de administrador. */
    public static String huellaDe(String texto) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Esta JVM no trae SHA-256, que exige el estándar de Java", e);
        }
    }

    /**
     * Si el código recibido corresponde a la huella guardada. Compara en
     * tiempo constante: con una comparación normal, medir cuánto tarda la
     * respuesta revelaría cuántos caracteres coinciden.
     *
     * Sin código o sin huella nunca coincide. Eso cubre los casos viejos, que
     * no tienen huella, y un código de administrador vacío.
     */
    public static boolean coincide(String codigoRecibido, String huellaGuardada) {
        if (codigoRecibido == null || codigoRecibido.isBlank() || huellaGuardada == null || huellaGuardada.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                huellaDe(codigoRecibido).getBytes(StandardCharsets.US_ASCII),
                huellaGuardada.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public String toString() {
        return "CodigoGestion[oculto]";
    }
}
