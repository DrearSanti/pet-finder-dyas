---
paths:
  - "src/main/java/petfinder/domain/**"
  - "src/main/java/petfinder/application/**"
---

# Núcleo: dominio y aplicación

- **Java puro.** Ni `org.springframework`, ni `jakarta.persistence`, ni `com.anthropic`. Sin anotaciones de Spring en servicios: se crean con `@Bean` en `config/`. `ReglasArquitecturaTest` falla si esto se rompe.
- El dominio no importa nada de `application` ni de `adaptadores`.
- Los puertos de entrada (`application/port/entrada`) hablan en tipos del dominio (`SolicitudReporte`, `ReporteMascota`, `Avistamiento`, `BorradorReporte`), nunca en DTO.
- Los puertos de salida (`application/port/salida`) son interfaces pequeñas: `RepositorioReportes` y `ExtractorDatosReporte`. Un adaptador nuevo implementa uno de ellos; el servicio no cambia.
- Objetos de valor como `record` con validación en el constructor compacto (`Ubicacion`, `BorradorReporte`).
- Sin setters en entidades. Las transiciones de estado viven en `EstadoReporte`; una transición inválida lanza `OperacionNoPermitidaException`.
- Errores de negocio: `DatosInvalidosException`, `ReporteNoEncontradoException`, `OperacionNoPermitidaException` (todas extienden `DominioException`) con mensaje en español para la persona.
- Reconstrucción desde la base: `ReportePerdida.reconstruir(...)` y `ReporteEncontrada.reconstruir(...)` usan el constructor protegido de `ReporteMascota`; no pasan por los creadores del Factory Method (el dato ya fue validado al crearse).
- `ServicioAvistamientos` guarda **antes** de notificar a los observadores, y una operación rechazada no notifica. No cambies ese orden.
- `ServicioAsistente` nunca recibe `GestionReportes`: el asistente arma borradores, no publica.
- Javadoc que explica el porqué de una decisión, no qué hace el método.
