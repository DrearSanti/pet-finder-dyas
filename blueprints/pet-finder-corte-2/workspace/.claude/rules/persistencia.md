---
paths:
  - "src/main/java/petfinder/adaptadores/salida/persistencia/**"
---

# Persistencia

- Las entidades JPA (`ReporteEntity`, `AvistamientoEntity`) son del adaptador y nunca salen de él: el puerto `RepositorioReportes` entrega y recibe objetos del dominio.
- El mapeo dominio ↔ entidad vive dentro de `RepositorioReportesH2`, en un solo lugar.
- `RepositorioReportesH2` **no lleva `@Repository`**: el bean se crea en `ConfiguracionPetFinder`. Con la anotación habría dos beans del mismo puerto.
- Colecciones: al actualizar avistamientos usa `clear()` y luego `add(...)` sobre la colección existente; reemplazar la lista deja huérfanos y Hibernate falla.
- Métodos que escriben llevan `@Transactional` en el adaptador (nunca en el servicio).
- Consultas solo con Spring Data derivadas o JPQL con parámetros; nunca concatenes texto en una consulta.
- Base: `jdbc:h2:mem:petfinder`; el esquema lo crea Hibernate. No agregues Flyway ni cambies el motor.
- `RepositorioReportesEnMemoria` se queda: lo usan las pruebas unitarias.
- Prueba el adaptador con `@DataJpaTest` e `@Import` de una `@TestConfiguration` que crea el bean.
