---
paths:
  - "src/main/java/petfinder/adaptadores/entrada/web/**"
---

# Adaptador web

- Los controladores reciben **puertos de entrada** por constructor (`GestionReportes`, `RegistroAvistamientos`, `ProcesadorComandosVoz`, `AsistenteReportes`). Nunca importan `application.service` ni nada de persistencia o IA.
- Un controlador traduce: JSON → tipo del dominio → puerto → DTO. Sin reglas de negocio aquí.
- DTO como `record` en `dto/`. `ReporteDTO` tiene dos fábricas: `resumen(...)` (listas y mapa) y `detalle(...)` (un caso). El `instanceof` para saber el tipo vive solo ahí. El JSON usa los nombres del contrato de §5 del blueprint; no los cambies.
- Errores: solo `ManejadorErrores` (`@RestControllerAdvice`) los convierte. Cuerpo siempre `{"error": "<mensaje en español>"}`.
  - `DatosInvalidosException` y JSON ilegible → 400 · `ReporteNoEncontradoException` → 404 · `OperacionNoPermitidaException` → 409 · voz no entendida → 422.
- Privacidad: en listas y mapa el contacto va enmascarado y las coordenadas pasan por `Ubicacion.aproximada()`. El contacto completo solo sale en `GET /api/reportes/{id}`.
- Rutas bajo `/api/`. Nada bajo `/api/` se sirve desde `static/`.
- Prueba cada controlador con `@WebMvcTest` y los puertos simulados (`@MockitoBean`).
