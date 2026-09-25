package petfinder.application.service;

import java.util.List;
import java.util.Optional;

import petfinder.application.port.entrada.AsistenteReportes;
import petfinder.application.port.salida.ExtractorDatosReporte;
import petfinder.domain.model.BorradorReporte;

/**
 * Un turno de conversación sobre la tarjeta viva.
 *
 * No recibe GestionReportes a propósito: por construcción no puede publicar.
 * El asistente solo arma el borrador; publicar es siempre un POST que la
 * persona dispara desde la tarjeta, después de verla completa.
 *
 * Recibe los extractores ya ordenados y usa el primero que responda. Así el
 * de Claude puede ir delante del de regex sin que este servicio cambie: si
 * Claude falla, se acaba el tope diario o no hay clave, el turno sigue con
 * el siguiente y la persona nunca se queda sin asistente.
 */
public class ServicioAsistente implements AsistenteReportes {

    static final String FUENTE_NINGUNA = "ninguna";
    static final String MENSAJE_LISTO = "Listo. Revisa la tarjeta y publica.";

    private final List<ExtractorDatosReporte> extractores;

    public ServicioAsistente(List<ExtractorDatosReporte> extractores) {
        this.extractores = List.copyOf(extractores);
    }

    @Override
    public ResultadoTurno procesarTurno(BorradorReporte actual, String texto) {
        BorradorReporte borrador = actual != null ? actual : BorradorReporte.vacio();
        if (texto == null || texto.isBlank()) {
            return resultado(borrador, FUENTE_NINGUNA);
        }
        for (ExtractorDatosReporte extractor : extractores) {
            Optional<BorradorReporte> extraido = intentar(extractor, texto, borrador);
            if (extraido.isPresent()) {
                return resultado(completarDescripcion(borrador.fusionar(extraido.get()), extraido.get(), texto),
                        extractor.nombre());
            }
        }
        return resultado(borrador, FUENTE_NINGUNA);
    }

    /**
     * Un extractor caído no debe tumbar el turno: cualquier RuntimeException
     * (red, límite, respuesta mal formada) equivale a "este no pudo".
     */
    private static Optional<BorradorReporte> intentar(ExtractorDatosReporte extractor, String texto,
                                                      BorradorReporte contexto) {
        try {
            Optional<BorradorReporte> extraido = extractor.extraer(texto, contexto);
            return extraido != null ? extraido : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Quien cuenta cómo perdió su mascota ya dio la descripción del caso: si
     * el turno trajo el tipo y la tarjeta aún no tiene descripción, la frase
     * misma lo es. Evita preguntar "cuéntame qué pasó" a quien acaba de contarlo.
     */
    private static BorradorReporte completarDescripcion(BorradorReporte fusionado, BorradorReporte extraido,
                                                        String texto) {
        boolean sinDescripcion = fusionado.descripcion() == null || fusionado.descripcion().isBlank();
        if (extraido.tipo() == null || !sinDescripcion) {
            return fusionado;
        }
        return fusionado.fusionar(new BorradorReporte(null, null, null, null, null, null, null,
                null, null, null, null, texto.strip(), null, null));
    }

    private static ResultadoTurno resultado(BorradorReporte borrador, String fuente) {
        List<BorradorReporte.Campo> faltantes = borrador.camposFaltantes();
        String pregunta = faltantes.isEmpty() ? MENSAJE_LISTO : faltantes.get(0).pregunta();
        return new ResultadoTurno(borrador, faltantes, pregunta, fuente);
    }
}
