package petfinder.application.port.entrada;

import petfinder.domain.model.ReporteMascota;

/**
 * Lo que devuelve publicar: el reporte y su código de gestión en claro.
 *
 * Es la única vez que el código sale del sistema. Después solo queda su
 * huella en la base, así que quien publica tiene que guardarlo: la API lo
 * entrega en la respuesta de publicar y nunca en la lista ni en el detalle.
 */
public record ReportePublicado(ReporteMascota reporte, String codigoGestion) {

    @Override
    public String toString() {
        return "ReportePublicado[" + reporte.getId() + ", código oculto]";
    }
}
