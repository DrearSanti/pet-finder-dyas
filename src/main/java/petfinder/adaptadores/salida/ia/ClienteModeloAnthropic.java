package petfinder.adaptadores.salida.ia;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;

/**
 * Única clase del proyecto que habla con el SDK de Anthropic. Todo lo demás
 * (el extractor, el servicio, el dominio) ignora que Claude existe: si mañana
 * el proveedor cambia, se cambia este archivo y nada más.
 *
 * El modelo llega por constructor y nunca se escribe aquí: se elige con
 * PETFINDER_IA_MODELO en ConfiguracionIa.
 *
 * Los límites son deliberadamente estrictos. Diez segundos y cero reintentos,
 * porque el respaldo (regex) responde al instante y es mejor caer a él que
 * hacer esperar a una persona con la tarjeta a medias.
 */
public class ClienteModeloAnthropic implements ExtractorClaude.ClienteModelo {

    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);
    private static final long MAXIMO_DE_TOKENS = 1024L;

    private static final List<String> CAMPOS = List.of("tipo", "nombre", "especie", "raza", "color", "senas",
            "descripcionMascota", "zona", "referencia", "latitud", "longitud", "descripcion", "contactoNombre", "contactoMedio");

    private final AnthropicClient cliente;
    private final String modelo;

    public ClienteModeloAnthropic(String claveApi, String modelo) {
        this.cliente = AnthropicOkHttpClient.builder()
                .apiKey(claveApi)
                .timeout(TIEMPO_MAXIMO)
                .maxRetries(0)
                .build();
        this.modelo = modelo;
    }

    /**
     * Lanza ante cualquier respuesta que no sea un final normal (rechazo,
     * corte por longitud): ExtractorClaude lo convierte en "este extractor no
     * pudo" y el turno sigue con el respaldo. No se incluye el texto de la
     * respuesta en el error para no filtrar lo que dijo la persona.
     */
    @Override
    public String completarJson(String sistema, String mensaje) {
        MessageCreateParams peticion = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(MAXIMO_DE_TOKENS)
                .system(sistema)
                .addUserMessage(mensaje)
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.LOW)
                        .format(JsonOutputFormat.builder().schema(esquema()).build())
                        .build())
                .build();
        Message respuesta = cliente.messages().create(peticion);
        if (!respuesta.stopReason().map(StopReason.END_TURN::equals).orElse(false)) {
            throw new IllegalStateException("La respuesta de Claude no terminó con normalidad: "
                    + respuesta.stopReason().map(Object::toString).orElse("sin motivo"));
        }
        return respuesta.content().stream()
                .flatMap(bloque -> bloque.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining());
    }

    /**
     * Fuerza la forma de la salida: catorce campos, con coordenadas numéricas
     * o null, los demás de texto o null y el tipo limitado a PERDIDA o ENCONTRADA.
     * El modelo solo puede llenar la tarjeta, no inventar campos nuevos.
     */
    private static JsonOutputFormat.Schema esquema() {
        JsonOutputFormat.Schema.Builder constructor = JsonOutputFormat.Schema.builder();
        definicionDelEsquema().forEach((clave, valor) -> constructor.putAdditionalProperty(clave, JsonValue.from(valor)));
        return constructor.build();
    }

    /**
     * El esquema como mapa, aparte del SDK, para poder revisarlo en una prueba.
     *
     * El tipo va con anyOf (texto de la lista, o null) y no con
     * "type": ["string", "null"] más un enum: la API rechaza ese enum con un
     * 400 ("Enum value 'PERDIDA' does not match declared type"). Las pruebas
     * usan un doble del cliente, así que ese error solo apareció con una clave
     * real; ClienteModeloAnthropicTest lo cuida desde entonces.
     */
    static Map<String, Object> definicionDelEsquema() {
        Map<String, Object> propiedades = new LinkedHashMap<>();
        for (String campo : CAMPOS) {
            propiedades.put(campo, switch (campo) {
                case "tipo" -> Map.of("anyOf", List.of(
                            Map.of("type", "string", "enum", List.of("PERDIDA", "ENCONTRADA")),
                            Map.of("type", "null")));
                case "latitud", "longitud" -> Map.of("type", List.of("number", "null"));
                default -> Map.of("type", List.of("string", "null"));
            });
        }
        Map<String, Object> esquema = new LinkedHashMap<>();
        esquema.put("type", "object");
        esquema.put("additionalProperties", false);
        esquema.put("properties", propiedades);
        esquema.put("required", CAMPOS);
        return esquema;
    }
}
