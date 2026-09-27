package petfinder.adaptadores.entrada.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.TipoAvistamiento;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Forma pública de un reporte en el JSON. El dominio nunca sale directo por
 * HTTP: así un cambio en el modelo no rompe el navegador, y lo que se expone de
 * cada persona se decide en un solo lugar.
 *
 * Hay dos vistas porque hay dos públicos. {@link #resumen} alimenta la lista y
 * el mapa, que ve cualquiera: coordenadas aproximadas y contacto enmascarado.
 * {@link #detalle} es la página de un caso: muestra el contacto completo porque
 * quien encuentra a la mascota necesita llamar. Los avistamientos nunca llevan
 * el contacto de quien avisó; la única excepción es {@link HallazgoDTO}: quien
 * dice tener a la mascota ("La tengo yo") deja su contacto para que la familia
 * vaya por ella, y va aparte para que esa exposición sea explícita.
 */
public record ReporteDTO(
        String id,
        TipoReporte tipo,
        EstadoReporte estado,
        LocalDateTime fechaCreacion,
        String zona,
        String referencia,
        Double latitud,
        Double longitud,
        String descripcion,
        String nombreMascota,
        String especie,
        String raza,
        String color,
        String senas,
        String descripcionMascota,
        String nombreContacto,
        String contacto,
        int cantidadAvistamientos,
        boolean laTieneAlguien,
        HallazgoDTO hallazgo,
        List<AvistamientoPublicoDTO> avistamientos) {

    private static final String PUNTO = "•";
    private static final String OCULTO = "•••";

    /** Un avistamiento tal como se muestra: sin nombre ni medio de quien lo reportó, con su tipo (pista o hallazgo). */
    public record AvistamientoPublicoDTO(
            String id,
            LocalDateTime fechaHora,
            String zona,
            String referencia,
            Double latitud,
            Double longitud,
            String descripcion,
            TipoAvistamiento tipo) {

        static AvistamientoPublicoDTO desde(Avistamiento avistamiento) {
            Ubicacion ubicacion = avistamiento.ubicacion();
            return new AvistamientoPublicoDTO(avistamiento.id(), avistamiento.fechaHora(),
                    ubicacion.zonaOBarrio(), ubicacion.referencia(),
                    ubicacion.latitud(), ubicacion.longitud(), avistamiento.descripcion(), avistamiento.tipo());
        }
    }

    /**
     * "La tengo yo": el último aviso de alguien que tiene a la mascota, con su
     * contacto. Solo en el detalle del caso, nunca en la lista ni en el mapa.
     */
    public record HallazgoDTO(
            LocalDateTime fechaHora,
            String zona,
            String referencia,
            String descripcion,
            String nombreContacto,
            String contacto) {

        static HallazgoDTO desde(Avistamiento avistamiento) {
            Ubicacion ubicacion = avistamiento.ubicacion();
            Contacto quien = avistamiento.contactoReportante();
            return new HallazgoDTO(avistamiento.fechaHora(), ubicacion.zonaOBarrio(), ubicacion.referencia(),
                    avistamiento.descripcion(), quien.nombre(), quien.medioContacto());
        }
    }

    /** Vista para listas y mapa: coordenadas a unos 100 m, contacto enmascarado y sin avistamientos. */
    public static ReporteDTO resumen(ReporteMascota reporte) {
        return construir(reporte, true);
    }

    /** Vista de un caso: ubicación exacta, contacto completo, avistamientos sin contacto y el hallazgo si lo hay. */
    public static ReporteDTO detalle(ReporteMascota reporte) {
        return construir(reporte, false);
    }

    /*
     * El instanceof vive solo aquí: es el único punto donde la web necesita
     * saber qué clase concreta de reporte tiene en la mano.
     */
    private static ReporteDTO construir(ReporteMascota reporte, boolean publico) {
        Ubicacion ubicacion = publico ? reporte.getUbicacion().aproximada() : reporte.getUbicacion();
        TipoReporte tipo;
        Mascota mascota = null;
        String descripcionMascota = null;
        Contacto contacto;
        List<Avistamiento> avistamientos = List.of();
        boolean laTieneAlguien = false;

        if (reporte instanceof ReportePerdida perdida) {
            tipo = TipoReporte.PERDIDA;
            mascota = perdida.getMascota();
            contacto = perdida.getContactoPropietario();
            avistamientos = perdida.getAvistamientos();
            laTieneAlguien = perdida.laTieneAlguien();
        } else if (reporte instanceof ReporteEncontrada encontrada) {
            tipo = TipoReporte.ENCONTRADA;
            descripcionMascota = encontrada.getDescripcionMascota();
            contacto = encontrada.getContactoReportante();
        } else {
            throw new IllegalArgumentException("Tipo de reporte sin vista web: " + reporte.getClass().getName());
        }

        String nombreContacto = contacto == null ? null : contacto.nombre();
        String medio = contacto == null ? null : contacto.medioContacto();
        HallazgoDTO hallazgo = publico ? null : avistamientos.stream()
                .filter(Avistamiento::esHallazgo)
                .reduce((anterior, ultimo) -> ultimo)
                .map(HallazgoDTO::desde)
                .orElse(null);
        List<AvistamientoPublicoDTO> avistamientosPublicos = publico
                ? List.of()
                : avistamientos.stream().map(AvistamientoPublicoDTO::desde).toList();

        return new ReporteDTO(
                reporte.getId(),
                tipo,
                reporte.getEstado(),
                reporte.getFechaCreacion(),
                ubicacion.zonaOBarrio(),
                ubicacion.referencia(),
                ubicacion.latitud(),
                ubicacion.longitud(),
                reporte.getDescripcion(),
                mascota == null ? null : mascota.nombre(),
                mascota == null ? null : mascota.especie(),
                mascota == null ? null : mascota.raza(),
                mascota == null ? null : mascota.color(),
                mascota == null ? null : mascota.senasParticulares(),
                descripcionMascota,
                nombreContacto,
                publico ? enmascarar(medio) : medio,
                avistamientos.size(),
                laTieneAlguien,
                hallazgo,
                avistamientosPublicos);
    }

    /**
     * Oculta el medio de contacto para una vista pública, dejando lo justo para
     * que la dueña reconozca que es su número o su correo:
     * teléfono → 3 primeros dígitos, un punto por cada dígito del medio y los
     * 2 últimos ({@code 3001234567} → {@code 300•••••67}); correo → 2 primeros
     * caracteres y el dominio ({@code so•••@correo.co}); cualquier otro texto de
     * más de 5 caracteres → sus 2 primeros; si es más corto no se muestra nada.
     */
    public static String enmascarar(String medio) {
        if (medio == null || medio.isBlank()) {
            return medio;
        }
        String texto = medio.strip();
        String digitos = texto.replaceAll("[\\s()+.-]", "");
        if (digitos.matches("\\d{6,}")) {
            int ocultos = digitos.length() - 5;
            return digitos.substring(0, 3) + PUNTO.repeat(ocultos) + digitos.substring(digitos.length() - 2);
        }
        int arroba = texto.indexOf('@');
        if (arroba > 0) {
            return texto.substring(0, Math.min(2, arroba)) + OCULTO + texto.substring(arroba);
        }
        if (texto.length() > 5) {
            return texto.substring(0, 2) + OCULTO;
        }
        return OCULTO;
    }
}
