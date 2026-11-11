package petfinder.adaptadores.entrada.web.dto;

import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.CORTO;
import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.LARGO;
import static petfinder.adaptadores.entrada.web.dto.LongitudCampos.exigir;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Cuerpo de {@code POST /api/reportes}: el formulario, la voz y el asistente
 * publican con este mismo JSON.
 *
 * Es un espejo plano del formulario y no del modelo: la persona escribe zona y
 * contacto como campos sueltos, y aquí se arman los objetos de valor del
 * dominio. Este DTO no valida reglas de negocio (zona obligatoria, nombre de la
 * mascota…): esas viven en los creadores del Factory Method, para que la
 * consola y la web rechacen exactamente lo mismo.
 */
public record SolicitudReporteDTO(
        TipoReporte tipo,
        String zona,
        String referencia,
        Double latitud,
        Double longitud,
        String descripcion,
        String nombreContacto,
        String medioContacto,
        MascotaDTO mascota,
        String descripcionMascota) {

    /** Datos de la mascota perdida; en un reporte de encontrada llega {@code null}. */
    public record MascotaDTO(String nombre, String especie, String raza, String color, String senas) {

        Mascota aMascota() {
            return new Mascota(exigir(nombre, CORTO, "nombre de la mascota"), exigir(especie, CORTO, "especie"),
                    exigir(raza, CORTO, "raza"), exigir(color, CORTO, "color"), exigir(senas, CORTO, "señas"));
        }
    }

    /**
     * Tipo del reporte, obligatorio. Se revisa aquí y no en el servicio porque
     * sin tipo no hay creador que lo valide, y el mensaje debe llegar a la
     * persona como un 400 y no como un fallo interno.
     */
    public TipoReporte tipoReporte() {
        if (tipo == null) {
            throw new DatosInvalidosException("Indica si la mascota se perdió o si la encontraste");
        }
        return tipo;
    }

    /**
     * Traduce el JSON a la solicitud del dominio usando la fábrica que
     * corresponde al tipo: una pérdida lleva la {@link Mascota}; una encontrada,
     * solo la descripción de lo que se vio. Antes revisa que cada texto quepa
     * en su columna (ver {@link LongitudCampos}).
     */
    public SolicitudReporte aSolicitud() {
        Ubicacion ubicacion = new Ubicacion(exigir(zona, CORTO, "zona"), exigir(referencia, CORTO, "referencia"),
                latitud, longitud);
        Contacto contacto = new Contacto(exigir(nombreContacto, CORTO, "nombre de contacto"),
                exigir(medioContacto, CORTO, "medio de contacto"));
        String detalle = exigir(descripcion, LARGO, "descripción");
        if (tipoReporte() == TipoReporte.PERDIDA) {
            Mascota datosMascota = mascota == null ? null : mascota.aMascota();
            return SolicitudReporte.paraPerdida(ubicacion, detalle, contacto, datosMascota);
        }
        return SolicitudReporte.paraEncontrada(ubicacion, detalle, contacto,
                exigir(descripcionMascota, LARGO, "descripción de la mascota"));
    }
}
