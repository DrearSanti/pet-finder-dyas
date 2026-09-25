package petfinder.domain.model;

import java.util.ArrayList;
import java.util.List;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Lo que la persona lleva dicho o escrito antes de publicar: la "tarjeta viva".
 *
 * Todos los campos pueden estar vacíos. camposFaltantes() aplica las mismas
 * reglas que exigen los creadores del Factory Method, así la voz, el
 * asistente y el formulario terminan en la misma validación y ninguno se la
 * salta.
 */
public record BorradorReporte(
        TipoReporte tipo,
        String nombre,
        String especie,
        String raza,
        String color,
        String senas,
        String descripcionMascota,
        String zona,
        String referencia,
        Double latitud,
        Double longitud,
        String descripcion,
        String contactoNombre,
        String contactoMedio) {

    /** Datos que el creador exige, en el orden en que el asistente los pregunta. */
    public enum Campo {
        TIPO("¿Perdiste una mascota o encontraste una?"),
        NOMBRE("¿Cómo se llama tu mascota?"),
        ESPECIE("¿Es perro, gato u otro animal?"),
        DESCRIPCION_MASCOTA("¿Cómo es el animal que encontraste?"),
        ZONA("¿En qué barrio o zona pasó?"),
        DESCRIPCION("Cuéntame en una frase qué pasó."),
        CONTACTO("¿A qué número o correo te pueden escribir?");

        private final String pregunta;

        Campo(String pregunta) {
            this.pregunta = pregunta;
        }

        public String pregunta() {
            return pregunta;
        }
    }

    public static BorradorReporte vacio() {
        return new BorradorReporte(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    public List<Campo> camposFaltantes() {
        List<Campo> faltantes = new ArrayList<>();
        if (tipo == null) {
            faltantes.add(Campo.TIPO);
        } else if (tipo == TipoReporte.PERDIDA) {
            if (vacio(nombre)) {
                faltantes.add(Campo.NOMBRE);
            }
            if (vacio(especie)) {
                faltantes.add(Campo.ESPECIE);
            }
        } else if (vacio(descripcionMascota)) {
            faltantes.add(Campo.DESCRIPCION_MASCOTA);
        }
        if (vacio(zona)) {
            faltantes.add(Campo.ZONA);
        }
        if (vacio(descripcion)) {
            faltantes.add(Campo.DESCRIPCION);
        }
        if (vacio(contactoMedio)) {
            faltantes.add(Campo.CONTACTO);
        }
        return List.copyOf(faltantes);
    }

    public boolean estaCompleto() {
        return camposFaltantes().isEmpty();
    }

    /** Lo nuevo gana solo donde trae un valor; lo que ya se había dicho se conserva. */
    public BorradorReporte fusionar(BorradorReporte nuevo) {
        if (nuevo == null) {
            return this;
        }
        return new BorradorReporte(
                nuevo.tipo != null ? nuevo.tipo : tipo,
                elegir(nuevo.nombre, nombre),
                elegir(nuevo.especie, especie),
                elegir(nuevo.raza, raza),
                elegir(nuevo.color, color),
                elegir(nuevo.senas, senas),
                elegir(nuevo.descripcionMascota, descripcionMascota),
                elegir(nuevo.zona, zona),
                elegir(nuevo.referencia, referencia),
                nuevo.latitud != null ? nuevo.latitud : latitud,
                nuevo.longitud != null ? nuevo.longitud : longitud,
                elegir(nuevo.descripcion, descripcion),
                elegir(nuevo.contactoNombre, contactoNombre),
                elegir(nuevo.contactoMedio, contactoMedio));
    }

    /** Convierte el borrador en la misma SolicitudReporte que arma el formulario. */
    public SolicitudReporte aSolicitud() {
        if (tipo == null) {
            throw new DatosInvalidosException("Falta indicar si la mascota se perdió o se encontró");
        }
        Ubicacion ubicacion = new Ubicacion(zona, referencia, latitud, longitud);
        Contacto contacto = new Contacto(contactoNombre, contactoMedio);
        if (tipo == TipoReporte.PERDIDA) {
            return SolicitudReporte.paraPerdida(ubicacion, descripcion, contacto,
                    new Mascota(nombre, especie, raza, color, senas));
        }
        return SolicitudReporte.paraEncontrada(ubicacion, descripcion, contacto, descripcionMascota);
    }

    private static String elegir(String nuevo, String actual) {
        return vacio(nuevo) ? actual : nuevo;
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
