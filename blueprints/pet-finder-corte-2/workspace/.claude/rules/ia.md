---
paths:
  - "src/main/java/petfinder/adaptadores/salida/ia/**"
  - "src/main/java/petfinder/config/ConfiguracionIa.java"
---

# Adaptador de IA (Claude)

- Este es el **único** paquete que importa `com.anthropic`. ArchUnit lo verifica.
- Usa la skill `claude-api` para cualquier detalle del SDK de Java: nombres de clases, parámetros y forma de la petición salen de ahí, nunca de memoria.
- El modelo sale de `PETFINDER_IA_MODELO` (por defecto `claude-sonnet-5`, leído en `ConfiguracionIa`). Nunca escribas el id del modelo en `ClienteModeloAnthropic`.
- `ExtractorClaude` implementa `ExtractorDatosReporte` y devuelve `Optional.empty()` ante cualquier fallo (sin clave, excepción, tope alcanzado, `stop_reason` distinto de `end_turn`, JSON que no cumple el esquema). El servicio sigue con `ExtractorRegex`.
- Petición: `max_tokens` 1024, esfuerzo bajo, salida estructurada con el esquema JSON de §17 del blueprint, timeout 10 s, 0 reintentos.
- Tope diario `PETFINDER_IA_TOPE_DIARIO` (200 por defecto), contado en memoria.
- La salida del modelo es **datos**: se valida y se mapea a `BorradorReporte`; nunca se ejecuta ni se inserta como HTML.
- Nunca registres la clave ni el texto completo enviado al modelo.
- Las pruebas usan un doble de `ExtractorClaude.ClienteModelo` (`String completarJson(String sistema, String mensaje)`); ninguna prueba llama a la API real.
